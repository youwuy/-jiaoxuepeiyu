# Student API Contract

All student APIs use the common response envelope:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

Compatibility identity fallback:

- Older frontend calls can still send `X-User-Id: <studentId>`.
- Backend returns `401` business code when the header is missing or invalid.

Preferred authenticated identity:

- Send `Authorization: Bearer <token>` after `POST /api/auth/student/login`.
- Token-authenticated requests no longer need `X-User-Id`; the header remains as a compatibility fallback while older frontend calls are migrated.

## Auth

### `POST /api/auth/student/login`

Request body:

```json
{
  "loginType": "studentNo",
  "account": "student001",
  "password": "configured-password"
}
```

`loginType` accepts `studentNo`, `username`, or `phone`.

Response `data`:

- `token`
- `expiresAt`
- `user`

### `GET /api/auth/current`

Header:

- `Authorization: Bearer <token>`

Response `data`: current authenticated user.

### `POST /api/auth/logout`

Header:

- `Authorization: Bearer <token>`

Invalidates the current token session and marks the user offline. Response `data`: `null`.

### `PUT /api/auth/password`

Header:

- `Authorization: Bearer <token>`

Request body:

```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

Behavior:

- Validates the current password before updating.
- New password must be `8-20` characters and contain letters and digits.
- New password cannot equal the current password.
- Stores only the new password hash and never returns plaintext passwords or password hashes.
- Existing `PUT /api/student/profile/password` remains available for older student clients.

## Online Presence

### `POST /api/online/heartbeat`

Header:

- `Authorization: Bearer <token>`
- Compatibility fallback: `X-User-Id`

Response `data`:

```json
{
  "heartbeatAt": "2026-07-30T18:00:00",
  "heartbeatIntervalSeconds": 30,
  "offlineTimeoutSeconds": 120
}
```

Behavior:

- Updates the current user's heartbeat time and IP address.
- Student clients should call this every `30` seconds while active.
- A student is treated as offline after `120` seconds without heartbeat or immediately after logout.

### `POST /api/online/offline`

Header:

- `Authorization: Bearer <token>`
- Compatibility fallback: `X-User-Id`

Clears the current user's heartbeat immediately. Response `data`: `null`.

## Course Learning

### `GET /api/student/courses`

Query:

- `keyword` optional course name keyword.

Response `data`: array of course items.

- `courseId`
- `courseName`
- `status`: `STUDYING`, `NOT_STARTED`, or `FINISHED`
- `academicTerm`
- `progressPercent`
- `coursewareCount`
- `assignmentCount`
- `teacherNames`
- `openStartTime`
- `openEndTime`

### `GET /api/student/courses/{courseId}`

Response `data`:

- `courseId`
- `courseName`
- `status`
- `academicTerm`
- `progressPercent`
- `teacherNames`
- `openStartTime`
- `openEndTime`
- `lastContentId`
- `chapters`

Each `chapters[]` item:

- `chapterId`
- `chapterTitle`
- `sortOrder`
- `items`

Each `items[]` item:

- `contentId`
- `itemType`: `COURSEWARE` or `ASSIGNMENT`
- `title`
- `resourceId`
- `assignmentId`
- `requiredDurationSeconds`
- `studiedSeconds`
- `completed`
- `sortOrder`

### `POST /api/student/courses/{courseId}/progress`

Request body:

```json
{
  "contentId": 1,
  "studiedSeconds": 300,
  "completed": true
}
```

Behavior:

- Only accepts courseware progress for courses visible to the current student.
- Stores the highest studied duration for the student/content pair.
- Marks the content complete when `completed` is true or studied duration reaches the configured required duration.

## Public Resources

### `GET /api/student/resources/public`

Query:

- `keyword` optional resource name keyword.
- `resourceType` optional: `DOCUMENT`, `PRESENTATION`, `IMAGE`, `VIDEO`, or `AUDIO`.
- `majorId` optional.

Response `data`: array of public resource cards.

Each item:

- `resourceId`
- `resourceName`
- `resourceType`
- `coverUrl`
- `previewUrl`
- `fileUrl`
- `fileName`
- `fileSize`
- `majorId`
- `majorName`
- `uploaderName`
- `updatedAt`

## Assignments

### `GET /api/student/assignments/{assignmentId}`

Response `data`:

- `assignmentId`
- `courseId`
- `assignmentTitle`
- `assignmentType`: `THEORY` or `TRAINING`
- `deadline`
- `totalScore`
- `status`: `NOT_STARTED`, `SAVED`, `SUBMITTED`, or `REVIEWED`
- `submittedAt`
- `questions`

Each `questions[]` item:

- `questionId`
- `questionType`: `SINGLE`, `MULTIPLE`, `JUDGE`, `FILL`, or `SHORT`
- `title`
- `score`
- `options`
- `answerContent`

### `POST /api/student/assignments/{assignmentId}/answers`

Request body:

```json
{
  "answers": [
    {
      "questionId": 1,
      "answerContent": "A"
    }
  ]
}
```

Behavior:

- Saves or replaces draft answers before submission.
- Rejects changes after the deadline or after the assignment has been submitted.

### `POST /api/student/assignments/{assignmentId}/submit`

Behavior:

- Locks the answer set.
- Auto-scores objective questions by exact answer match.
- Leaves short-answer questions for manual review.

Response `data`:

- `attemptId`
- `status`
- `autoScore`
- `submittedAt`

### `GET /api/student/assignments/{assignmentId}/report`

Response `data`:

- `assignmentId`
- `assignmentTitle`
- `status`
- `score`
- `reviewComment`
- `submittedAt`
- `answers`

Each `answers[]` item:

- `questionId`
- `questionType`
- `title`
- `standardAnswer`
- `answerContent`
- `score`

## Scores

### `GET /api/student/scores/semester`

Response `data`: array of semester score items.

Each item:

- `academicTerm`
- `coursewareLearningScore`
- `trainingPracticeScore`
- `courseAssignmentScore`
- `examScore`
- `coursewareWeight`
- `trainingPracticeWeight`
- `assignmentWeight`
- `examWeight`
- `comprehensiveScore`

Behavior:

- Returns current student's semester scores ordered by term descending.
- Calculates `comprehensiveScore` with the configured four component weights when a stored summary score is not present.

## Training Center

### `GET /api/student/trainings`

Query:

- `mode` optional: `SINGLE` or `TEAM`
- `keyword` optional training name keyword.

Response `data`: array of training items.

- `trainingId`
- `trainingName`
- `trainingMode`: `SINGLE` or `TEAM`
- `status`: `NOT_STARTED`, `RUNNING`, or `FINISHED`
- `openStartTime`
- `openEndTime`
- `teamSize`
- `roleCount`
- `appRequired`
- `appInstalled`
- `activeRoomId`

### `GET /api/student/trainings/app-installation`

Response `data`:

- `installed`
- `version`
- `downloadUrl`
- `message`

### `POST /api/student/trainings/{trainingId}/rooms`

Behavior:

- Creates a room for a team training.
- Rejects the request when the student is already in another active room.
- Adds the creator as room owner and member.

Response `data`: same as `GET /api/student/training-rooms/{roomId}`.

### `GET /api/student/training-rooms/{roomId}`

Response `data`:

- `roomId`
- `trainingId`
- `trainingName`
- `roomCode`
- `roomStatus`: `WAITING`, `STARTED`, or `DISSOLVED`
- `ownerStudentId`
- `teamSize`
- `members`
- `roles`

Each `members[]` item:

- `studentId`
- `studentName`
- `roleId`
- `roleName`
- `owner`

Each `roles[]` item:

- `roleId`
- `roleName`
- `claimed`
- `claimedByStudentId`

### `POST /api/student/training-rooms/{roomId}/join`

Behavior:

- Rejects joining when the student is already in another active room.
- Rejects joining when the room is full or no longer waiting.

### `POST /api/student/training-rooms/{roomId}/leave`

Behavior:

- Releases the student's claimed role.
- Dissolves the room when the owner leaves before start.

### `POST /api/student/training-rooms/{roomId}/roles/{roleId}/claim`

Behavior:

- Claims an unclaimed role for the current student.
- Rejects conflicts when another member already claimed the role.

### `POST /api/student/training-rooms/{roomId}/start`

Behavior:

- Only the owner can start.
- Requires the room to be full and every member to have a role.
- Changes room status to `STARTED`.

UE launch and callback integration:

- `GET /api/ue/trainings/{trainingId}/task` returns launch metadata for the current student.
- `POST /api/ue/trainings/{trainingId}/status` reports live status.
- `POST /api/ue/trainings/{trainingId}/attempts` submits scores, steps, and recording metadata.
- Full callback contract is documented in `docs/ue-api-contract.md`.

## Training Archives

### `GET /api/student/archives`

Query:

- `mode` optional: `SINGLE` or `TEAM`
- `keyword` optional training name keyword.

Response `data`: array of training archive items.

Each item:

- `archiveId`
- `trainingName`
- `trainingMode`: `SINGLE` or `TEAM`
- `roleName`
- `submittedAt`
- `submitType`: `NORMAL`, `ABNORMAL_EXIT`, or `ROOM_DISSOLVED`
- `durationSeconds`
- `personalScore`
- `teamScore`

### `GET /api/student/archives/{archiveId}`

Response `data`:

- `archiveId`
- `trainingName`
- `trainingMode`
- `roleName`
- `studentName`
- `studentNo`
- `className`
- `submittedAt`
- `submitType`
- `durationSeconds`
- `personalScore`
- `teamScore`
- `recordingUrl`
- `steps`

Each `steps[]` item:

- `stepId`
- `stepName`
- `standardOperation`
- `actualOperation`
- `score`
- `durationSeconds`
- `videoStartSecond`
