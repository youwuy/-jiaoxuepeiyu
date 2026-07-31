# Admin API Contract

All admin APIs use the common response envelope:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

Online OpenAPI documentation is available after backend startup:

- `GET /v3/api-docs`
- `GET /swagger-ui.html`

Compatibility identity fallback:

- `X-User-Id`

Preferred authenticated identity:

- Send `Authorization: Bearer <token>` after login.
- Token-authenticated requests no longer need `X-User-Id`; the header remains as a compatibility fallback while older frontend calls are migrated.

## Health

### `GET /api/health`

Public deployment smoke-check endpoint.

Response `data`:

- `status`: `OK`.
- `service`: `jiaoxuepeiyu-backend`.
- `javaVersion`: runtime Java version.
- `databaseVersionTarget`: `MySQL 5.7.42.0`.
- `time`: current server time.

## Auth

### First Admin Bootstrap

There is no committed default administrator password.

For the first deployment startup only, configure:

- `APP_BOOTSTRAP_ADMIN_USERNAME`
- `APP_BOOTSTRAP_ADMIN_PASSWORD`
- Optional: `APP_BOOTSTRAP_ADMIN_REAL_NAME`
- Optional: `APP_BOOTSTRAP_ADMIN_PHONE`

Behavior:

- The backend creates one `admin` user only when both username and password are configured and no admin user exists.
- If only username or only password is configured, startup fails.
- If an admin already exists, bootstrap is skipped and existing users are not changed.
- The password must be `8-20` characters and contain letters and digits.
- Only the hashed password is stored.

Remove the bootstrap username and password environment variables after the first successful startup. Student and teacher initial passwords are still controlled by `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password`.

### `POST /api/auth/admin/login`

Request body:

```json
{
  "loginType": "username",
  "account": "teacher001",
  "password": "configured-password"
}
```

`loginType` accepts `username`, `employeeNo`, or `phone`.

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

## Admin Profile

### `GET /api/admin/profile`

Header:

- `Authorization: Bearer <token>`
- Compatibility fallback: `X-User-Id`

Response `data`:

- `userId`
- `accountNo`
- `realName`
- `userType`: `admin` or `teacher`.
- `phone`: masked phone number.
- `idCard`: masked ID card number.
- `orgName`
- `jobTitle`

### `PUT /api/admin/profile/phone`

Header:

- `Authorization: Bearer <token>`
- Compatibility fallback: `X-User-Id`

Request body:

```json
{
  "phone": "13812345678"
}
```

Behavior:

- Updates the current admin or teacher phone number.
- Phone must be an 11-digit number.

### `PUT /api/admin/profile/id-card`

Header:

- `Authorization: Bearer <token>`
- Compatibility fallback: `X-User-Id`

Request body:

```json
{
  "idCard": "110101199001011234"
}
```

Behavior:

- Updates the current admin or teacher ID card number.
- ID card must match the configured 18-character ID format.

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

- Updates `sys_user.last_heartbeat_time` and `sys_user.last_login_ip`.
- Clients should call this every `30` seconds while active.
- Users with no heartbeat for `120` seconds are treated as offline.

### `POST /api/online/offline`

Header:

- `Authorization: Bearer <token>`
- Compatibility fallback: `X-User-Id`

Clears the current user's heartbeat immediately. Response `data`: `null`.

### `GET /api/admin/online/users`

Query:

- `userType` optional: `admin`, `teacher`, or `student`.
- `keyword` optional fuzzy username, real name, or phone.
- `onlineOnly` optional boolean.
- `limit` optional, default `100`, maximum `500`.

Response `data`:

- `generatedAt`
- `totalCount`
- `onlineCount`
- `offlineCount`
- `heartbeatIntervalSeconds`
- `offlineTimeoutSeconds`
- `users`

Each `users[]` item:

- `userId`
- `username`
- `realName`
- `userType`
- `lastLoginIp`
- `lastHeartbeatTime`
- `online`

## Files

### `POST /api/files`

Consumes: `multipart/form-data`.

Header:

- `Authorization: Bearer <token>`

Form fields:

- `file` required multipart file.
- `category` optional directory category. Accepted characters: letters, digits, `_`, and `-`. Blank defaults to `general`.

Response `data`:

```json
{
  "fileUrl": "/uploads/resources/5d41402abc4b2a76b9719d911017c592.pdf",
  "fileName": "lesson.pdf",
  "storedFileName": "5d41402abc4b2a76b9719d911017c592.pdf",
  "fileSize": 1048576,
  "contentType": "application/pdf",
  "category": "resources"
}
```

Behavior:

- Stores the file under configurable `app.file.upload-root`.
- Serves uploaded files from configurable `app.file.public-prefix`, default `/uploads`.
- Rejects empty files, files larger than configured limits, and unsafe categories.
- Requires an authenticated admin, teacher, or student token.
- Use this endpoint before creating or updating resource metadata; copy `fileUrl`, `fileName`, and `fileSize` into `POST /api/admin/resources`.

## Organization

### `GET /api/admin/org/tree`

Query:

- `keyword` optional fuzzy organization name.
- `enabledOnly` optional boolean. Use `true` for enabled parent organization picker options.

Response `data`: array of root organization nodes.

Each node:

- `orgId`
- `parentId`
- `orgName`
- `sortOrder`
- `enabled`
- `createdBy`
- `createdName`
- `updatedBy`
- `updatedName`
- `createdAt`
- `updatedAt`
- `children`

### `POST /api/admin/org`

Request body:

```json
{
  "parentId": null,
  "orgName": "School",
  "sortOrder": 1
}
```

Behavior:

- `orgName` is required, trimmed, and limited to 20 characters.
- Sibling organizations under the same parent cannot use the same name.
- New organizations are enabled by default.
- Child organizations can only be created under enabled parent organizations.
- Creator/updater and create/update time are filled by the backend.

### `PUT /api/admin/org/{orgId}`

Request body: same as create.

Behavior:

- Updates organization name and sort order.
- Parent organization is read-only and cannot be changed.

### `PUT /api/admin/org/sort`

Request body:

```json
[
  {
    "orgId": 1,
    "sortOrder": 1
  }
]
```

Persists drag-and-drop organization sort order.

### `POST /api/admin/org/{orgId}/enable`

Enables the selected organization only. Descendant organization statuses are unchanged.

### `POST /api/admin/org/{orgId}/disable`

Disables the selected organization and all descendant organizations.

## Account Management

### `GET /api/admin/accounts/teachers`

Query:

- `orgId` optional.
- `realName` optional fuzzy match.
- `accountNo` optional fuzzy match.
- `phone` optional fuzzy match.
- `enabled` optional boolean.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of teacher accounts.

### `GET /api/admin/accounts/students`

Query:

- `orgId` optional.
- `classId` optional.
- `realName` optional fuzzy match.
- `accountNo` optional fuzzy match.
- `phone` optional fuzzy match.
- `enabled` optional boolean.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of student accounts.

### `GET /api/admin/accounts/{userId}`

Response `data`: account detail.

Sensitive fields are returned masked:

- `maskedPhone`
- `maskedIdCard`

### `POST /api/admin/accounts/teachers`

Request body:

```json
{
  "realName": "Teacher One",
  "accountNo": "teacher001",
  "phone": "13812345678",
  "idCard": "110101199001011234",
  "jobTitle": "Teacher",
  "orgId": 1,
  "managedOrgIds": [1, 2],
  "teachingClassIds": [3],
  "roleIds": [4],
  "faceFileId": null,
  "fingerprintFileId": null
}
```

Behavior:

- Creates a `teacher` user.
- Uses `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password` to create the password hash.
- Does not return the plaintext initial password.

### `POST /api/admin/accounts/students`

Request body:

```json
{
  "realName": "Student One",
  "accountNo": "student001",
  "phone": "13812345678",
  "idCard": "110101199001011234",
  "orgId": 1,
  "classId": 3,
  "faceFileId": null,
  "fingerprintFileId": null
}
```

Behavior:

- Creates a `student` user.
- `classId` is required.
- Uses `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password` to create the password hash.

### `POST /api/admin/accounts/teachers/import/preview`

Request body:

```json
{
  "rows": [
    {
      "rowNo": 2,
      "accountNo": "teacher001",
      "realName": "Teacher One",
      "phone": "13812345678",
      "idCard": "110101199001011234",
      "jobTitle": "Teacher",
      "orgId": 1,
      "roleIds": [4],
      "managedOrgIds": [1, 2],
      "teachingClassIds": [3]
    }
  ]
}
```

Response `data`:

- `totalCount`
- `validCount`
- `errorCount`
- `rows`: normalized import rows with `valid` and row-level `errors`.

Behavior:

- Validates already parsed teacher account rows.
- Checks required account number, name, valid phone, organization, optional ID card format, existing account number, and duplicate account numbers within submitted rows.
- Binary Excel parsing is intentionally outside this endpoint; frontend or import adapters submit parsed rows as JSON.

### `POST /api/admin/accounts/teachers/import`

Request body: same as teacher import preview.

Response `data`:

- `importedCount`
- `userIds`

Behavior:

- Re-validates all rows before writing.
- Creates teacher accounts through the same validation, role binding, organization scope binding, and initial-password hashing path as `POST /api/admin/accounts/teachers`.
- Uses `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password`.
- Does not return plaintext initial passwords.
- Rejects the whole import when any row has validation errors.

### `GET /api/admin/accounts/teachers/export`

Query: same filters as `GET /api/admin/accounts/teachers`.

Response `data`: array of export-ready teacher account rows.

Each row:

- `userId`
- `accountNo`
- `realName`
- `maskedPhone`
- `maskedIdCard`
- `userType`
- `orgName`
- `className`
- `jobTitle`
- `enabled`
- `createdAt`

Behavior:

- Returns all rows matching filters, ordered by newest account first.
- Sensitive phone and ID card fields are masked.

### `GET /api/admin/accounts/teachers/export/file`

Query: same filters as `GET /api/admin/accounts/teachers`.

Response: `text/csv;charset=UTF-8` attachment named `teacher-accounts.csv`.

Behavior:

- Generates an Excel-compatible UTF-8 BOM CSV file.
- Uses the same masked export rows as `GET /api/admin/accounts/teachers/export`.

### `POST /api/admin/accounts/students/import/preview`

Request body:

```json
{
  "rows": [
    {
      "rowNo": 2,
      "accountNo": "student001",
      "realName": "Student One",
      "phone": "13812345678",
      "idCard": "110101199001011234",
      "orgId": 1,
      "classId": 3
    }
  ]
}
```

Response `data`: same shape as teacher import preview.

Behavior:

- Validates already parsed student account rows.
- Checks required account number, name, valid phone, organization, class, optional ID card format, existing account number, and duplicate account numbers within submitted rows.
- Binary Excel parsing is intentionally outside this endpoint; frontend or import adapters submit parsed rows as JSON.

### `POST /api/admin/accounts/students/import`

Request body: same as student import preview.

Response `data`:

- `importedCount`
- `userIds`

Behavior:

- Re-validates all rows before writing.
- Creates student accounts through the same validation and initial-password hashing path as `POST /api/admin/accounts/students`.
- Uses `APP_ACCOUNT_INITIAL_PASSWORD` / `app.account.initial-password`.
- Does not return plaintext initial passwords.
- Rejects the whole import when any row has validation errors.

### `GET /api/admin/accounts/students/export`

Query: same filters as `GET /api/admin/accounts/students`.

Response `data`: array of export-ready student account rows.

Behavior:

- Returns all rows matching filters, ordered by newest account first.
- Sensitive phone and ID card fields are masked.

### `GET /api/admin/accounts/students/export/file`

Query: same filters as `GET /api/admin/accounts/students`.

Response: `text/csv;charset=UTF-8` attachment named `student-accounts.csv`.

Behavior:

- Generates an Excel-compatible UTF-8 BOM CSV file.
- Uses the same masked export rows as `GET /api/admin/accounts/students/export`.

### `PUT /api/admin/accounts/teachers/{userId}`

Request body: same as teacher create.

Behavior:

- Updates teacher profile fields except `accountNo`.
- Replaces role, managed organization, and teaching class bindings.

### `PUT /api/admin/accounts/students/{userId}`

Request body: same as student create.

Behavior:

- Updates student profile fields except `accountNo`.

### `POST /api/admin/accounts/{userId}/enable`

Enables an account login.

### `POST /api/admin/accounts/{userId}/disable`

Disables an account login.

### `POST /api/admin/accounts/batch/reset-password`

Request body:

```json
{
  "userIds": [1, 2]
}
```

Behavior:

- Resets selected accounts to the configured initial password hash.
- Does not return the plaintext initial password.

### `POST /api/admin/accounts/batch/org`

Request body:

```json
{
  "userIds": [1, 2],
  "orgId": 3
}
```

### `PUT /api/admin/accounts/teachers/{userId}/roles`

Request body:

```json
{
  "roleIds": [1, 2]
}
```

## Education Configuration

### `GET /api/admin/academic-years`

Response `data`: array of academic years with semesters.

Each item:

- `academicYearId`
- `yearName`
- `semesters`

Each semester:

- `semesterId`
- `semesterName`: `FIRST` or `SECOND`
- `current`

### `POST /api/admin/academic-years`

Request body:

```json
{
  "yearName": "2026-2027"
}
```

Behavior:

- Creates the academic year.
- Creates `FIRST` and `SECOND` semesters for the year.

### `POST /api/admin/semesters/{semesterId}/current`

Behavior:

- Clears `current_flag` for all semesters.
- Marks the requested semester as current.

### `GET /api/admin/majors`

Response `data`: array of majors.

### `POST /api/admin/majors`

Request body:

```json
{
  "majorName": "Urban Rail Transit"
}
```

### `POST /api/admin/majors/{majorId}/enable`

Enables a major.

### `POST /api/admin/majors/{majorId}/disable`

Disables a major.

### `GET /api/admin/classes`

Query:

- `majorId` optional.

Response `data`: array of classes.

### `POST /api/admin/classes`

Request body:

```json
{
  "majorId": 1,
  "className": "Class 2026-01"
}
```

### `GET /api/admin/job-roles`

Response `data`: array of subway job role dictionary rows.

Each item:

- `jobRoleId`
- `roleName`
- `sortOrder`
- `enabled`

### `POST /api/admin/job-roles`

Request body:

```json
{
  "roleName": "Driver",
  "sortOrder": 1
}
```

Behavior:

- `roleName` is required, trimmed, and limited to `20` characters.
- `sortOrder` defaults to `0` when omitted.
- New job roles are enabled by default.
- `roleName` is unique.

### `PUT /api/admin/job-roles/{jobRoleId}`

Request body: same as create.

Behavior:

- Updates the subway job role name and sort order.
- Historical account and training data are preserved.

### `POST /api/admin/job-roles/{jobRoleId}/enable`

Enables a subway job role for future selection.

### `POST /api/admin/job-roles/{jobRoleId}/disable`

Disables a subway job role while preserving historical records.

## Facility Configuration

### `GET /api/admin/classrooms`

Response `data`: array of classrooms with cameras.

### `GET /api/admin/classrooms/{classroomId}`

Response `data`: classroom detail with camera list.

### `POST /api/admin/classrooms`

Request body:

```json
{
  "roomName": "Training Room A",
  "cameras": [
    {
      "nvrHost": "10.0.0.1",
      "nvrPort": 554,
      "adminUsername": "admin",
      "adminPassword": "input-from-admin",
      "nvrChannel": "CH01",
      "streamUrl": "rtsp://10.0.0.1/live/ch01"
    }
  ]
}
```

Behavior:

- At least one camera is required.
- `nvrHost` must be IPv4.
- `nvrPort` must be between `1` and `65535`.
- The same classroom cannot contain duplicate `nvrHost + nvrChannel` pairs.

### `PUT /api/admin/classrooms/{classroomId}`

Request body: same as create.

Behavior:

- Updates the classroom name.
- Replaces the classroom camera list with the submitted list.

### `DELETE /api/admin/classrooms/{classroomId}`

Deletes the classroom and its camera records.

## Score Configuration

### `GET /api/admin/score-weights`

Query:

- `semesterId` optional.

Response `data`: score weight history sorted by newest effective time.

### `POST /api/admin/score-weights`

Header:

- `X-User-Id`: current admin user id.

Request body:

```json
{
  "semesterId": 1,
  "coursewareWeight": 20,
  "trainingPracticeWeight": 30,
  "assignmentWeight": 20,
  "examWeight": 30
}
```

Behavior:

- The four weights must add up to `100`.
- Creates a new score weight history row instead of mutating previous rows.

### `GET /api/admin/score-grade-rules`

Response `data`: score grade rules sorted from high score to low score.

### `PUT /api/admin/score-grade-rules`

Request body:

```json
[
  {
    "gradeName": "A",
    "minScore": 90,
    "maxScore": 100
  },
  {
    "gradeName": "B",
    "minScore": 80,
    "maxScore": 89.9
  }
]
```

Behavior:

- Replaces all score grade rules.
- Ranges must stay within `0` to `100`.
- Ranges cannot overlap.

## Resource Management

### `GET /api/admin/resources`

Query:

- `keyword` optional fuzzy resource name.
- `resourceType` optional: `DOCUMENT`, `PRESENTATION`, `IMAGE`, `VIDEO`, `AUDIO`.
- `majorId` optional.
- `courseName` optional fuzzy course text.
- `uploaderId` optional.
- `publicStatus` optional: `NOT_APPLIED`, `PENDING`, `PUBLIC`, `REJECTED`.
- `uploadStartDate` / `uploadEndDate` optional `YYYY-MM-DD`.
- `page` default `1`, `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of resource metadata.

### `POST /api/admin/resources`

Header:

- `X-User-Id`: current admin user id.

Request body:

```json
{
  "resourceName": "Safety Training",
  "coverUrl": "/uploads/covers/0cc175b9c0f1b6a831c399e269772661.png",
  "fileUrl": "/uploads/resources/92eb5ffee6ae2fec3ad71c777531578f.mp4",
  "previewUrl": "/uploads/previews/4a8a08f09d37b73795649038408b5f33.mp4",
  "fileName": "intro.mp4",
  "fileSize": 1048576,
  "majorId": 1,
  "courseName": "Train Ops"
}
```

Behavior:

- `resourceName`, `coverUrl`, `fileUrl`, `fileName`, `fileSize`, and `majorId` are required.
- `resourceName` cannot exceed `20` characters; `courseName` cannot exceed `30` characters.
- File size cannot exceed `200MB`.
- File suffix determines resource type.
- Upload file content with `POST /api/files` first; this API stores only metadata and version snapshots.
- Initial public status is `NOT_APPLIED`.
- Creates version `1` and a resource operation log.

### `PUT /api/admin/resources/{resourceId}`

Request body: same as create.

Behavior:

- Updates metadata and creates the next resource version.
- If an older version is already public, the old public version remains visible until the new version is approved.

### `PUT /api/admin/resources/batch`

Request body:

```json
{
  "resourceIds": [1, 2],
  "coverUrl": "https://cdn.example/cover.png",
  "majorId": 3,
  "courseName": "Train Ops"
}
```

Behavior:

- At least one resource id is required.
- At least one of `coverUrl`, `majorId`, or `courseName` must be provided.
- Empty fields are ignored instead of clearing existing values.

### `POST /api/admin/resources/batch/delete`

Request body:

```json
{
  "resourceIds": [1, 2]
}
```

Behavior:

- Rejects deletion when any selected resource is already bound to a course.
- Uses soft delete for personal resources.
- Public resource rows remain available after personal resource deletion.

### `POST /api/admin/resources/{resourceId}/public-applications`

Header:

- `X-User-Id`: current admin user id.

Behavior:

- Submits the current resource version for public review.
- Rejects duplicate pending applications for the same resource.
- Marks the personal resource public status as `PENDING`.

### `GET /api/admin/resources/{resourceId}/logs`

Response `data`: resource operation logs sorted by newest first.

### `GET /api/admin/public-applications`

Query: same resource filters as `GET /api/admin/resources`; `publicStatus` means review status: `PENDING`, `APPROVED`, or `REJECTED`.

Response `data`: `PageResponse` of public review applications.

### `GET /api/admin/public-applications/{applicationId}`

Response `data`: public review application detail with submitted version snapshot.

### `POST /api/admin/public-applications/{applicationId}/approve`

Header:

- `X-User-Id`: current admin user id.

Request body:

```json
{
  "reviewComment": "OK"
}
```

Behavior:

- Only pending applications can be reviewed.
- Publishes the submitted version to `res_public_resource`.
- Replaces the previously public version for the same source resource.
- Sends a `RESOURCE` notification to all enabled students.

### `POST /api/admin/public-applications/{applicationId}/reject`

Request body:

```json
{
  "reviewComment": "Cover is unclear"
}
```

Behavior:

- Review comment is required.
- Marks the application as `REJECTED`.
- Keeps any older approved public version available.

### `GET /api/admin/public-resources`

Query: same resource filters as `GET /api/admin/resources`.

Response `data`: `PageResponse` of approved public resources. Only the latest approved version per source resource is returned.

## Exam Management

Question types:

- `SINGLE`
- `MULTIPLE`
- `JUDGE`
- `FILL_BLANK`
- `SHORT_ANSWER`

Paper compose modes:

- `MANUAL`
- `AUTO`

Paper publish status:

- `DRAFT`
- `PUBLISHED`
- `OFFLINE`

### `GET /api/admin/questions`

Query:

- `keyword` optional fuzzy title.
- `questionType` optional.
- `enabled` optional boolean.
- `creatorId` optional.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of question bank entries.

### `GET /api/admin/questions/{questionId}`

Response `data`: question detail with options.

### `POST /api/admin/questions`

Header:

- `X-User-Id`: current admin user id.

Request body:

```json
{
  "questionType": "SINGLE",
  "title": "Pick one",
  "score": 10,
  "options": [
    {
      "optionKey": "A",
      "optionText": "Alpha",
      "correct": true
    },
    {
      "optionKey": "B",
      "optionText": "Beta",
      "correct": false
    }
  ]
}
```

Behavior:

- `SINGLE` and `MULTIPLE` answers are derived from option `correct` flags.
- `SINGLE` must have exactly one correct option.
- `MULTIPLE` must have at least two correct options.
- `JUDGE` standard answer must be `TRUE` or `FALSE`.
- `FILL_BLANK` and `SHORT_ANSWER` require `standardAnswer`.

### `PUT /api/admin/questions/{questionId}`

Request body: same as create.

Behavior:

- Updates question content and replaces options.
- Does not alter historical paper question snapshots.

### `POST /api/admin/questions/{questionId}/enable`

Enables a question for future paper assembly.

### `POST /api/admin/questions/{questionId}/disable`

Disables a question for future paper assembly without deleting historical references.

### `POST /api/admin/questions/import/preview`

Request body:

```json
{
  "fileName": "questions.xlsx",
  "fileSize": 1024,
  "rows": [
    {
      "rowNumber": 2,
      "questionType": "SINGLE",
      "title": "Pick one",
      "score": 10,
      "options": [
        {
          "optionKey": "A",
          "optionText": "Alpha",
          "correct": true
        },
        {
          "optionKey": "B",
          "optionText": "Beta",
          "correct": false
        }
      ]
    }
  ]
}
```

Behavior:

- Validates already parsed Excel rows and returns `validCount`, `errorCount`, `validRows`, and row-level `errors`.
- Binary Excel parsing is intentionally left outside this metadata-first endpoint.

### `GET /api/admin/questions/{questionId}/logs`

Response `data`: question operation logs sorted by newest first.

### `GET /api/admin/papers`

Query:

- `keyword` optional fuzzy paper name.
- `composeMode` optional: `MANUAL` or `AUTO`.
- `publishStatus` optional: `DRAFT`, `PUBLISHED`, or `OFFLINE`.
- `creatorId` optional.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of theory papers.

### `GET /api/admin/papers/{paperId}`

Response `data`: paper detail with stored question snapshots.

### `POST /api/admin/papers`

Header:

- `X-User-Id`: current admin user id.

Manual request body:

```json
{
  "paperName": "Manual Paper",
  "composeMode": "MANUAL",
  "questions": [
    {
      "questionId": 1,
      "score": 5
    }
  ]
}
```

Auto request body:

```json
{
  "paperName": "Auto Paper",
  "composeMode": "AUTO",
  "autoRules": [
    {
      "questionType": "SINGLE",
      "questionCount": 10,
      "scorePerQuestion": 5
    }
  ]
}
```

Behavior:

- Manual papers reject duplicate, disabled, or missing questions.
- Auto papers randomly select enabled questions by type.
- Total score is computed from the submitted or generated paper questions.
- Paper question snapshots are stored so later question edits do not rewrite historical papers.

### `PUT /api/admin/papers/{paperId}`

Request body: same as create.

Behavior:

- Rebuilds paper question snapshots and total score.

### `POST /api/admin/papers/{paperId}/publish`

Behavior:

- Publishes a paper only when it contains at least one question.

### `POST /api/admin/papers/{paperId}/cancel-publish`

Behavior:

- Marks a paper as `OFFLINE`.

### `POST /api/admin/papers/import/preview`

Request body:

```json
{
  "fileName": "papers.xlsx",
  "fileSize": 2048,
  "rows": [
    {
      "rowNumber": 2,
      "paperName": "Manual Paper",
      "composeMode": "MANUAL",
      "questions": [
        {
          "questionId": 1,
          "score": 5
        }
      ]
    }
  ]
}
```

Behavior:

- Validates already parsed Excel rows and returns `validCount`, `errorCount`, `validRows`, and row-level `errors`.
- Manual rows validate duplicate, missing, disabled, and invalid-score questions.
- Auto rows validate question type, question count, score per question, and enabled question pool size.
- Binary Excel parsing is intentionally left outside this metadata-first endpoint.

### `GET /api/admin/papers/{paperId}/logs`

Response `data`: paper operation logs sorted by newest first.

## Teaching Courses

Course publish status:

- `DRAFT`
- `PUBLISHED`
- `OFFLINE`

Learning modes:

- `SELF_PACED`
- `TEACHER_LED`

Assignment completion rules:

- `SUBMIT`
- `PASS_SCORE`

Course content item types:

- `COURSEWARE`
- `ASSIGNMENT`

### `GET /api/admin/courses`

Query:

- `keyword` optional fuzzy course name.
- `academicYearId` optional.
- `semesterId` optional.
- `majorId` optional.
- `classId` optional.
- `teacherId` optional.
- `publishStatus` optional: `DRAFT`, `PUBLISHED`, or `OFFLINE`.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of teaching courses.

### `GET /api/admin/courses/{courseId}`

Response `data`: course detail with teacher ids, class ids, chapters, and content nodes.

### `POST /api/admin/courses`

Header:

- `X-User-Id`: current admin user id.

Request body:

```json
{
  "courseName": "Safety Course",
  "academicYearId": 1,
  "semesterId": 2,
  "majorId": 3,
  "coverUrl": "https://cdn.example/course.png",
  "openStartTime": "2026-09-01T00:00:00",
  "openEndTime": "2026-12-31T23:59:00",
  "teacherIds": [9],
  "classIds": [10, 11],
  "learningMode": "SELF_PACED",
  "assignmentCompletionRule": "SUBMIT",
  "coursewareScoreCap": 100,
  "chapters": [
    {
      "chapterTitle": "Chapter 1",
      "sortOrder": 1,
      "contents": [
        {
          "itemType": "COURSEWARE",
          "title": "Intro courseware",
          "resourceId": 1,
          "requiredDurationSeconds": 60,
          "sortOrder": 1
        },
        {
          "itemType": "ASSIGNMENT",
          "title": "Theory assignment",
          "assignmentId": 2,
          "sortOrder": 2
        }
      ]
    }
  ]
}
```

Behavior:

- Creates a draft course.
- At least one teacher and one teaching class are required.
- Course open end time must be later than open start time.
- Courseware nodes require `resourceId`.
- Assignment nodes require `assignmentId`.
- `coursewareCount` and `assignmentCount` are computed from submitted content nodes.
- Multi-class bindings are stored in `course_class`; `course.class_id` keeps the first class for legacy student queries.

### `PUT /api/admin/courses/{courseId}`

Request body: same as create.

Behavior:

- Updates course metadata.
- Fully replaces submitted teacher, class, chapter, and content bindings.
- Assignment content nodes update the linked assignment `course_id` and `content_id`.

### `POST /api/admin/courses/{courseId}/publish`

Behavior:

- Rejects empty courses.
- Marks the course `PUBLISHED`.
- Sends a `COURSE` notification to enabled students in bound classes.

### `POST /api/admin/courses/{courseId}/cancel-publish`

Behavior:

- Marks the course `OFFLINE`.

### `POST /api/admin/courses/{courseId}/delete`

Behavior:

- Soft deletes the course and marks it `OFFLINE`.

### `POST /api/admin/courses/{courseId}/copy`

Behavior:

- Copies the course metadata, teacher/class bindings, chapters, and content nodes into a new draft course.

### `GET /api/admin/courses/{courseId}/statistics`

Response `data`:

- `studentCount`
- `completedCount`
- `studyingCount`
- `notStartedCount`
- `pendingReviewCount`
- `averageScore`

### `GET /api/admin/courses/{courseId}/logs`

Response `data`: course operation logs sorted by newest first.

## Assignment Review

Attempt status:

- `SAVED`
- `SUBMITTED`
- `REVIEWED`

### `GET /api/admin/assignment-attempts`

Query:

- `courseId` optional.
- `assignmentId` optional.
- `classId` optional.
- `studentId` optional.
- `status` optional: `SUBMITTED` or `REVIEWED`.
- `keyword` optional fuzzy student name, student number, or assignment title.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of assignment attempts with student, class, course, assignment, score, and review state.

### `GET /api/admin/assignment-attempts/{attemptId}`

Response `data`: assignment attempt detail with question answers.

Each answer includes:

- `questionId`
- `questionType`
- `title`
- `standardAnswer`
- `answerContent`
- `questionScore`
- `score`
- `reviewComment`

### `POST /api/admin/assignment-attempts/{attemptId}/review`

Header:

- `X-User-Id`: current admin or teacher user id.

Request body:

```json
{
  "reviewComment": "Good work",
  "answers": [
    {
      "questionId": 1,
      "score": 8,
      "comment": "Clear answer"
    },
    {
      "questionId": 2,
      "score": 10,
      "comment": "OK"
    }
  ]
}
```

Behavior:

- Only `SUBMITTED` or previously `REVIEWED` attempts can be reviewed.
- Each answer score must be within `0` and its question score.
- Reviewed total score cannot exceed assignment total score.
- Marks the attempt `REVIEWED`, stores reviewer and review time, persists per-question score/comment, and refreshes course progress.

### `GET /api/admin/assignment-attempts/{attemptId}/logs`

Response `data`: review operation logs sorted by newest first.

## Training Management

Training type:

- `PRACTICE`
- `EXAM`

Training mode:

- `SINGLE`
- `TEAM`

Paper mode:

- `MANUAL`
- `AUTO`

Publish status:

- `DRAFT`
- `PUBLISHED`
- `OFFLINE`

### `GET /api/admin/trainings`

Query:

- `keyword` optional fuzzy training name.
- `academicYearId` optional.
- `semesterId` optional.
- `majorId` optional.
- `classId` optional.
- `trainingType` optional: `PRACTICE` or `EXAM`.
- `trainingMode` optional: `SINGLE` or `TEAM`.
- `publishStatus` optional: `DRAFT`, `PUBLISHED`, or `OFFLINE`.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of training courses with term, major, paper, class names, participant count, room count, and average score.

### `GET /api/admin/trainings/export`

Query: same filters as `GET /api/admin/trainings`.

Response `data`: export-ready training course rows for frontend-controlled export.

Behavior:

- Returns rows matching the submitted filters.
- Uses the same maximum export page size policy as other admin export endpoints.

### `GET /api/admin/trainings/export/file`

Query: same filters as `GET /api/admin/trainings`.

Response: `text/csv;charset=UTF-8` attachment named `trainings.csv`.

Behavior:

- Generates an Excel-compatible UTF-8 BOM CSV file.
- Uses the same filtered export rows as `GET /api/admin/trainings/export`.

### `GET /api/admin/trainings/{trainingId}`

Response `data`: training detail with bound `classIds` and team `roles`.

### `POST /api/admin/trainings`

Header:

- `X-User-Id`: current admin or teacher user id.

Request body:

```json
{
  "trainingName": "Door Operation Drill",
  "academicYearId": 1,
  "semesterId": 2,
  "majorId": 3,
  "coverUrl": "https://cdn.example/training.png",
  "trainingType": "PRACTICE",
  "trainingMode": "TEAM",
  "paperMode": "MANUAL",
  "paperId": 5,
  "openStartTime": "2026-09-01T00:00:00",
  "openEndTime": "2026-12-31T23:59:00",
  "teamSize": 2,
  "appRequired": true,
  "classIds": [10, 11],
  "roles": [
    {
      "roleName": "Driver",
      "sortOrder": 1
    },
    {
      "roleName": "Dispatcher",
      "sortOrder": 2
    }
  ]
}
```

Behavior:

- Creates a draft training course.
- Training name, academic year, semester, major, cover, open time range, and at least one class are required.
- `trainingType` defaults to `PRACTICE`; `trainingMode` defaults to `SINGLE`; `paperMode` defaults to `MANUAL`.
- Manual paper mode requires `paperId`; exam training also requires `paperId`.
- Single training uses `teamSize = 1` and cannot configure team roles.
- Team training requires `teamSize > 1`, and submitted role count must match `teamSize`.

### `PUT /api/admin/trainings/{trainingId}`

Request body: same as create.

Behavior:

- Updates training metadata.
- Fully replaces submitted class bindings and role definitions.

### `POST /api/admin/trainings/{trainingId}/publish`

Behavior:

- Rejects publishing when bound classes have no enabled students.
- Revalidates exam paper and team role constraints.
- Rebuilds `training_participant` from enabled students in bound classes.
- Marks the training `PUBLISHED`.
- Sends a `TRAINING` notification to participants.

### `POST /api/admin/trainings/{trainingId}/cancel-publish`

Behavior:

- Marks the training `OFFLINE`.
- Preserves participants, rooms, monitor rows, and logs.

### `POST /api/admin/trainings/{trainingId}/delete`

Behavior:

- Soft deletes the training and marks it `OFFLINE`.

### `GET /api/admin/trainings/{trainingId}/statistics`

Response `data`:

- `participantCount`
- `waitingRoomCount`
- `startedRoomCount`
- `dissolvedRoomCount`
- `submittedAttemptCount`
- `averageScore`
- `maxScore`
- `minScore`

### `GET /api/admin/trainings/{trainingId}/monitor`

Response `data`:

- `generatedAt`
- `statistics`
- `cameras`: classroom camera stream and online state.
- `students`: student desk state, progress state, room state, role, and score.

Monitor rows are read from `training_monitor_snapshot`; UE callbacks update that table through `POST /api/ue/trainings/{trainingId}/status` and `POST /api/ue/trainings/{trainingId}/attempts`.

### `GET /api/admin/trainings/{trainingId}/logs`

Response `data`: training operation logs sorted by newest first.

## Device Efficiency

Device status:

- `OFFLINE`
- `IDLE`
- `IN_USE`
- `FAULT`

Device type examples:

- `TRAINING_TERMINAL`
- `VR`
- `CONTROL_DESK`
- `OTHER`

Common query:

- `startDate` optional `YYYY-MM-DD`; defaults to the first day of the current month when both dates are omitted.
- `endDate` optional `YYYY-MM-DD`; defaults to today when omitted.
- `classroomId` optional.
- `deviceType` optional.
- `deviceStatus` optional.
- `rankLimit` optional for ranking endpoints, default `10`, maximum `100`.

Behavior:

- Date ranges are inclusive.
- The maximum query window is `366` days.
- Real-time state is read from `device` and latest active `device_usage_event`.
- Historical usage, utilization, monthly trend, and heat ranking are read from `device_usage_daily_summary`.

### `GET /api/admin/devices/efficiency`

Response `data`:

- `summary`
- `realtimeStates`
- `monthlyTrends`
- `heatRanking`

This is the dashboard aggregate endpoint for the management device efficiency page.

### `GET /api/admin/devices/efficiency/summary`

Response `data`:

- `totalDeviceCount`
- `onlineDeviceCount`
- `activeDeviceCount`
- `faultDeviceCount`
- `totalUsageMinutes`
- `averageUtilizationRate`
- `activeTrainingCount`

### `GET /api/admin/devices/efficiency/realtime`

Response `data`: list of device real-time states.

Each item:

- `deviceId`
- `deviceCode`
- `deviceName`
- `deviceType`
- `deviceStatus`
- `classroomId`
- `classroomName`
- `currentTrainingId`
- `currentTrainingName`
- `currentStudentId`
- `currentStudentName`
- `currentStartedAt`
- `currentUsageMinutes`
- `lastHeartbeatAt`

### `GET /api/admin/devices/efficiency/monthly-trends`

Response `data`: list grouped by month.

Each item:

- `month`
- `usageMinutes`
- `usageCount`
- `utilizationRate`

### `GET /api/admin/devices/efficiency/heat-ranking`

Response `data`: devices sorted by usage heat.

Each item:

- `rankNo`
- `deviceId`
- `deviceCode`
- `deviceName`
- `deviceType`
- `classroomId`
- `classroomName`
- `usageMinutes`
- `usageCount`
- `utilizationRate`

## Semester Score Management

### `GET /api/admin/scores/semester`

Query:

- `semesterId` optional.
- `majorId` optional.
- `classId` optional.
- `studentId` optional.
- `keyword` optional fuzzy student name or student number.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of semester score rows.

Each row includes:

- student, class, major, semester, and academic term fields.
- component scores and component weights.
- `comprehensiveScore`; when the stored value is empty, the backend returns a calculated value from component scores and weights.

### `GET /api/admin/scores/semester/statistics`

Response `data`:

- `studentCount`
- `averageScore`
- `maxScore`
- `minScore`
- `excellentCount`
- `passCount`

### `GET /api/admin/scores/semester/ranking`

Response `data`: score rows sorted by comprehensive score, each with `rankNo`.

### `GET /api/admin/scores/semester/export`

Response `data`: export-ready score rows for frontend-controlled export.

### `GET /api/admin/scores/semester/export/file`

Response: `text/csv;charset=UTF-8` attachment named `semester-scores.csv`.

Behavior:

- Generates an Excel-compatible UTF-8 BOM CSV file.
- Uses the same filters as `GET /api/admin/scores/semester`.

### `POST /api/admin/scores/semester/import/preview`

Request body:

```json
{
  "rows": [
    {
      "rowNo": 2,
      "studentNo": "student001",
      "semesterId": 1,
      "coursewareLearningScore": 80,
      "trainingPracticeScore": 90,
      "courseAssignmentScore": 85,
      "examScore": 88,
      "coursewareWeight": 20,
      "trainingPracticeWeight": 30,
      "assignmentWeight": 20,
      "examWeight": 30
    }
  ]
}
```

Response `data`:

- `totalCount`
- `validCount`
- `errorCount`
- `rows`: normalized import rows with `studentId`, calculated `comprehensiveScore`, `valid`, and row-level `errors`.

Behavior:

- Validates already parsed offline score rows.
- `studentNo` must belong to an enabled student account.
- `semesterId` must exist.
- Component scores and weights are required and must be between `0` and `100`.
- The four weights must add up to `100`.
- Duplicate `studentNo + semesterId` rows in the same import are rejected.
- Binary Excel parsing is intentionally outside this endpoint; frontend or import adapters submit parsed rows as JSON.

### `POST /api/admin/scores/semester/import`

Request body: same as import preview.

Response `data`:

- `importedCount`

Behavior:

- Re-validates all rows before writing.
- Calculates `comprehensiveScore` on the backend using the submitted component scores and weights.
- Upserts `score_semester_summary` by `student_id + semester_id`.
- Sets `published_at` to the import time so imported scores are immediately visible in score lists and student score APIs.
- Rejects the whole import when any row has validation errors.

## Training Archive Management

### `GET /api/admin/archives`

Query:

- `trainingId` optional.
- `studentId` optional.
- `classId` optional.
- `trainingMode` optional: `SINGLE` or `TEAM`.
- `submitType` optional: `NORMAL`, `ABNORMAL_EXIT`, or `ROOM_DISSOLVED`.
- `keyword` optional fuzzy training name, student name, or student number.
- `submittedStartDate` optional `YYYY-MM-DD`.
- `submittedEndDate` optional `YYYY-MM-DD`.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of immutable training attempt archive rows.

### `GET /api/admin/archives/{archiveId}`

Response `data`: archive detail with student, class, training, scores, recording URL, and ordered step records.

Archive rows are created by UE result callbacks documented in `docs/ue-api-contract.md`.

### `GET /api/admin/archives/statistics`

Response `data`:

- `archiveCount`
- `normalSubmitCount`
- `abnormalSubmitCount`
- `roomDissolvedCount`
- `averagePersonalScore`
- `averageDurationSeconds`

### `GET /api/admin/archives/export`

Response `data`: export-ready archive rows for frontend-controlled export.

### `GET /api/admin/archives/export/file`

Response: `text/csv;charset=UTF-8` attachment named `training-archives.csv`.

Behavior:

- Generates an Excel-compatible UTF-8 BOM CSV file.
- Uses the same filters as `GET /api/admin/archives`.

## IAM Role And Permission Management

### `GET /api/admin/permissions/tree`

Response `data`: permission tree assembled from `sys_permission`.

Each node:

- `permissionId`
- `parentId`
- `permissionName`
- `permissionCode`
- `permissionType`: `MENU`, `PAGE`, or `BUTTON`.
- `routePath`
- `visible`
- `sortOrder`
- `children`

### `POST /api/admin/permissions`

Header:

- `Authorization: Bearer <token>` preferred.
- `X-User-Id`: temporary admin operator id fallback.

Request body:

- `parentId` optional parent permission id.
- `permissionName` required.
- `permissionCode` required and unique.
- `permissionType` required; accepts `MENU`, `PAGE`, or `BUTTON`.
- `routePath` optional frontend route path.
- `visible` optional boolean, defaults to `true`.
- `sortOrder` optional integer, defaults to `0`.

Response `data`: created permission id.

### `PUT /api/admin/permissions/{permissionId}`

Header:

- `Authorization: Bearer <token>` preferred.
- `X-User-Id`

Request body: same as permission create.

Rules:

- `permissionCode` must stay unique.
- A permission cannot use itself as parent.
- Role permission bindings are preserved.

Response `data`: `null`.

### `POST /api/admin/permissions/{permissionId}/enable`

Header:

- `Authorization: Bearer <token>` preferred.
- `X-User-Id`

Response `data`: `null`.

### `POST /api/admin/permissions/{permissionId}/disable`

Header:

- `Authorization: Bearer <token>` preferred.
- `X-User-Id`

Response `data`: `null`.

### `POST /api/admin/permissions/{permissionId}/delete`

Header:

- `Authorization: Bearer <token>` preferred.
- `X-User-Id`

Rules:

- Deletes only leaf permission nodes.
- Rejects deletion when the permission is bound to any role.

Response `data`: `null`.

### `GET /api/admin/roles`

Query:

- `keyword` optional fuzzy role name or role code.
- `enabled` optional boolean.
- `page` default `1`.
- `pageSize` default `20`, maximum `100`.

Response `data`: `PageResponse` of role rows.

Each row:

- `roleId`
- `roleName`
- `roleCode`
- `dataScope`: `PERSONAL`, `MANAGED_ORG`, or `ALL`.
- `remark`
- `enabled`
- `userCount`
- `permissionIds`
- `createdAt`
- `updatedAt`

### `GET /api/admin/roles/{roleId}`

Response `data`: one role row with bound `permissionIds`.

### `POST /api/admin/roles`

Header:

- `X-User-Id`: temporary admin operator id until token authentication is wired into a servlet filter.

Request body:

- `roleName` required.
- `roleCode` required and unique.
- `dataScope` optional, defaults to `PERSONAL`; accepts `PERSONAL`, `MANAGED_ORG`, or `ALL`.
- `remark` optional.
- `permissionIds` optional list of permission ids.

Response `data`: created role id.

### `PUT /api/admin/roles/{roleId}`

Header:

- `X-User-Id`

Request body: same as create role. The submitted permission list fully replaces existing bindings.

Response `data`: `null`.

### `POST /api/admin/roles/{roleId}/enable`

Header:

- `X-User-Id`

Response `data`: `null`.

### `POST /api/admin/roles/{roleId}/disable`

Header:

- `X-User-Id`

Response `data`: `null`.

### `POST /api/admin/roles/{roleId}/delete`

Header:

- `X-User-Id`

Soft deletes the role and disables it for future use. Existing `sys_user_role` rows are preserved for audit compatibility.

Response `data`: `null`.

### `PUT /api/admin/roles/{roleId}/permissions`

Header:

- `X-User-Id`

Request body:

- `permissionIds` optional list of permission ids. Duplicates, nulls, and non-positive ids are ignored.

Response `data`: `null`.

### `GET /api/admin/roles/{roleId}/logs`

Response `data`: list of role operation logs sorted by newest first.

Each log:

- `logId`
- `roleId`
- `operatorId`
- `operatorName`
- `action`
- `content`
- `createdAt`
