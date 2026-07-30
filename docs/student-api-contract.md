# Student API Contract

All student APIs use the common response envelope:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

Temporary integration identity:

- Frontend sends `X-User-Id: <studentId>` until token authentication is wired into a servlet filter.
- Backend returns `401` business code when the header is missing or invalid.

## Course Learning

### `GET /api/student/courses`

Query:

- `keyword` optional course name keyword.

Response `data`:

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
