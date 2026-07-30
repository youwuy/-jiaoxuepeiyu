# Admin API Contract

All admin APIs use the common response envelope:

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

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
