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

- Preferred: `Authorization: Bearer <student-token>`.
- Compatibility fallback: `X-User-Id: <studentId>`.
- The backend resolves the current student identity from the request. UE clients must not submit another student's id in the body.

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
  "paperId": 3,
  "openStartTime": "2026-07-30T08:00:00",
  "openEndTime": "2026-07-30T18:00:00",
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
- For team trainings, `aiRoleNames` contains unclaimed room role names so UE can mark those roles as AI-filled launch parameters.

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
      "durationSeconds": 40,
      "videoStartSecond": 5
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
- Defaults missing duration fields to `0`.
- Scores must be between `0` and `100` when present.
- Inserts one immutable `training_attempt` row and ordered `training_attempt_step` rows.
- Updates `training_monitor_snapshot` to `SUBMITTED` for `NORMAL`, otherwise `ABNORMAL`.
- Recording files can be uploaded with `POST /api/files` first, then passed as `recordingUrl`.
