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

Temporary integration identity header:

- `X-User-Id`

## Organization

### `GET /api/admin/org/tree`

Response `data`: array of root organization nodes.

Each node:

- `orgId`
- `parentId`
- `orgName`
- `sortOrder`
- `enabled`
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

### `PUT /api/admin/org/{orgId}`

Request body: same as create.

### `POST /api/admin/org/{orgId}/enable`

Enables an organization.

### `POST /api/admin/org/{orgId}/disable`

Disables an organization.

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
