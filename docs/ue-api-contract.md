# UE API Contract

UE callback APIs use the common response envelope:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

Authentication:

- Production UE client: `X-UE-Token: <launchToken>` issued by the launch-session API.
- Browser/student compatibility: `Authorization: Bearer <student-token>`.
- Temporary legacy fallback: `X-User-Id: <studentId>`.
- The backend resolves the current student identity from the request. UE clients must not submit another student's id in the body.

Scope:

- 当前三维实训只保留 4 个对接接口：获取实训任务、上传实训录屏、回传实时状态、提交实训成绩。
- 管理端实训档案、学生端实训档案读取三维成绩提交后写入的 `training_attempt` / `training_attempt_step`。
- 管理端综合成绩、学生端综合成绩中的“实训练习”成绩由三维成绩提交接口的 `personalScore` 同步写入当前学期综合成绩。

## Launch Session

### `POST /api/student/trainings/{trainingId}/launch-session?topicId={topicId}`

Requires the logged-in student's bearer token. It validates that the published training topic is assigned to the student and returns an eight-hour token scoped to that student, training, topic, and optional team room.

Response `data`:

```json
{
  "launchToken": "short-lived-token",
  "studentId": 7,
  "trainingId": 15,
  "topicId": 31,
  "roomId": 22,
  "expiresAt": "2026-08-05T22:00:00"
}
```

The web client opens the following registered protocol after creating the session:

```text
jiaoyu-ue://launch?protocolVersion=1&apiBase=http%3A%2F%2Fjiaoyu.luoyan.xin%2Fapi&trainingId=15&topicId=31&studentId=7&roomId=22&launchToken=...
```

The UE installer must register the `jiaoyu-ue` protocol and map these values to the executable's command-line arguments. UE then sends `X-UE-Token` on task, upload, status, and result requests.

## Training Task

### `GET /api/ue/trainings/{trainingId}/task`

Returns launch metadata for a published training assigned to the current student.

Response `data`:

```json
{
  "trainingId": 15,
  "trainingName": "Crane Practice",
  "trainingType": "PRACTICE",
  "trainingMode": "TEAM",
  "topicId": 31,
  "topicName": "Platform emergency handling",
  "paperId": 3,
  "openStartTime": "2026-07-30T08:00:00",
  "openEndTime": "2026-07-30T18:00:00",
  "examStartedAt": null,
  "studentId": 7,
  "studentName": "Student Seven",
  "roomId": 22,
  "roomCode": "ROOM-22",
  "roomStatus": "STARTED",
  "roleId": 5,
  "roleName": "Operator",
  "teamSize": 2,
  "aiRoleNames": ["Safety Officer"]
}
```

Behavior:

- Rejects trainings not assigned to the current student.
- Rejects unpublished or deleted trainings.
- For a started team exam, rejects students who did not already receive a room role before the administrator started the exam.
- For team trainings, `aiRoleNames` contains roles configured for AI and roles not selected by a human when the room starts. UE only displays those roles as AI-performed; this system does not implement AI behavior.
- Result submissions must use the topic bound to the launch token. A mismatched `topicId` is rejected.

## Recording Upload

### `POST /api/files`

Uploads a training recording file. Use `multipart/form-data`.

Request form fields:

- `file`: required recording file.
- `category`: optional, recommended value `recordings`.

Example response `data`:

```json
{
  "fileUrl": "/uploads/recordings/6d7e8f9a.mp4",
  "fileName": "training-run.mp4",
  "storedFileName": "6d7e8f9a.mp4",
  "fileSize": 2048576,
  "contentType": "video/mp4",
  "category": "recordings"
}
```

Behavior:

- Returns the public `fileUrl`.
- UE passes that `fileUrl` as `recordingUrl` when calling `POST /api/ue/trainings/{trainingId}/attempts`.
- Maximum upload size follows backend config `app.file.max-size-bytes`.

## Live Status Callback

### `POST /api/ue/trainings/{trainingId}/status`

Request body:

```json
{
  "classroomId": 3,
  "deskStatus": "ONLINE",
  "progressStatus": "RUNNING",
  "score": 37.5,
  "teamScore": null,
  "eventTime": "2026-07-30T18:15:30"
}
```

Allowed `deskStatus`:

- `OFFLINE`
- `ONLINE`
- `FAULT`

Allowed `progressStatus`:

- `NOT_STARTED`
- `RUNNING`
- `SUBMITTED`
- `ABNORMAL`

Behavior:

- Defaults `deskStatus` to `ONLINE`.
- Defaults `progressStatus` to `RUNNING`.
- Scores must be between `0` and `100` when present.
- Upserts `training_monitor_snapshot`, which is read by the admin training monitor page.

## Result Callback

### `POST /api/ue/trainings/{trainingId}/attempts`

Request body:

```json
{
  "clientAttemptId": "attempt-20260805-001",
  "submitType": "NORMAL",
  "durationSeconds": 480,
  "personalScore": 92.5,
  "teamScore": 88.0,
  "recordingUrl": "/uploads/recordings/run.mp4",
  "submittedAt": "2026-07-30T18:20:00",
  "steps": [
    {
      "stepName": "Power on",
      "standardOperation": "Turn on the simulator",
      "actualOperation": "Completed",
      "score": 10,
      "maxScore": 10,
      "durationSeconds": 40,
      "videoStartSecond": 5,
      "videoEndSecond": 45
    }
  ]
}
```

Allowed `submitType`:

- `NORMAL`
- `ABNORMAL_EXIT`
- `ROOM_DISSOLVED`

Response `data`: new training archive id.

Behavior:

- Defaults `submitType` to `NORMAL`.
- `clientAttemptId` is optional but strongly recommended. Repeating the same value for the same student and training returns the original archive id without creating duplicate scores.
- Defaults missing duration and video timeline fields to `0`; video start/end must be supplied together, and `videoEndSecond` cannot be before `videoStartSecond`.
- Each step requires `score` and `maxScore`; both must be between `0` and `100`, `maxScore` must be greater than `0`, and `score` cannot exceed `maxScore`.
- Inserts one immutable `training_attempt` row and ordered `training_attempt_step` rows.
- Step score and maximum score are used by management statistics to calculate the weak-step error rate as `1 - sum(score) / sum(maxScore)`.
- Updates `training_monitor_snapshot` to `SUBMITTED` for `NORMAL`, otherwise `ABNORMAL`.
- Synchronizes `personalScore` to `score_semester_summary.training_practice_score` for the current semester, so admin and student comprehensive scores use the UE-submitted training score.
- Recording files can be uploaded with `POST /api/files` first, then passed as `recordingUrl`.
