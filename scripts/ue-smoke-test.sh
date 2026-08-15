#!/usr/bin/env bash

set -euo pipefail

: "${BASE_URL:?Set BASE_URL, for example http://luoyan.xin/api}"
: "${STUDENT_TOKEN:?Set STUDENT_TOKEN to a logged-in student bearer token}"
: "${TRAINING_ID:?Set TRAINING_ID to an assigned published training id}"

command -v curl >/dev/null 2>&1 || { echo "curl is required" >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "jq is required" >&2; exit 1; }

task_dir="$(mktemp -d)"
trap 'rm -rf -- "$task_dir"' EXIT

base_url="${BASE_URL%/}"

curl -fsS -X POST \
  -H "Authorization: Bearer ${STUDENT_TOKEN}" \
  "${base_url}/student/trainings/${TRAINING_ID}/launch-session" \
  -o "${task_dir}/launch.json"

launch_token="$(jq -er '.data.launchToken' "${task_dir}/launch.json")"
echo "Launch session created."

curl -fsS \
  -H "X-UE-Token: ${launch_token}" \
  "${base_url}/ue/trainings/${TRAINING_ID}/task" \
  -o "${task_dir}/task.json"

jq '.data | {trainingId, trainingName, trainingMode, studentId, roomId, roleName, aiRoleNames}' "${task_dir}/task.json"

if [[ "${WRITE_TEST:-0}" != "1" ]]; then
  echo "Read-only UE smoke test passed. Set WRITE_TEST=1 to test status, recording upload, and score submission."
  exit 0
fi

curl -fsS -X POST \
  -H "Content-Type: application/json" \
  -H "X-UE-Token: ${launch_token}" \
  -d '{"deskStatus":"ONLINE","progressStatus":"RUNNING","score":37.5}' \
  "${base_url}/ue/trainings/${TRAINING_ID}/status" \
  -o "${task_dir}/status.json"

recording_url=""
if [[ -n "${RECORDING_FILE:-}" ]]; then
  curl -fsS -X POST \
    -H "X-UE-Token: ${launch_token}" \
    -F "file=@${RECORDING_FILE}" \
    -F "category=recordings" \
    "${base_url}/files" \
    -o "${task_dir}/upload.json"
  recording_url="$(jq -er '.data.fileUrl' "${task_dir}/upload.json")"
fi

client_attempt_id="ue-smoke-$(date +%Y%m%d%H%M%S)"
jq -n \
  --arg clientAttemptId "$client_attempt_id" \
  --arg recordingUrl "$recording_url" \
  '{clientAttemptId:$clientAttemptId,submitType:"NORMAL",durationSeconds:60,personalScore:86,teamScore:88,recordingUrl:($recordingUrl|select(length>0)),steps:[{stepName:"UE smoke test",standardOperation:"Start",actualOperation:"Completed",score:10,durationSeconds:10,videoStartSecond:0}]}' \
  > "${task_dir}/attempt.json"

curl -fsS -X POST \
  -H "Content-Type: application/json" \
  -H "X-UE-Token: ${launch_token}" \
  --data-binary "@${task_dir}/attempt.json" \
  "${base_url}/ue/trainings/${TRAINING_ID}/attempts" \
  -o "${task_dir}/attempt-response.json"

jq '.data' "${task_dir}/attempt-response.json"
echo "Writable UE smoke test passed."
