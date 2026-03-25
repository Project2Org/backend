# Calendar App — Backend

A RESTful backend for a calendar application built for **CST 438 Project 2**.

**Stack:** Java 21 · Spring Boot 3.5 · PostgreSQL (Supabase) · Gradle · Docker
**API Docs:** Swagger UI available at `/swagger-ui` when running

---

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd backend
```

### 2. Build the application

**Mac / Linux:**
```bash
./gradlew build
```

**Windows:**
```bash
.\gradlew.bat build
```

### 3. Build the Docker image (Have Docker Desktop open)

```bash
docker build -t calendar-backend .
```

### 4. Run the backend

```bash
docker run -p 8080:8080 calendar-backend
```

The API will be available at **http://localhost:8080**  
Swagger UI will be available at **http://localhost:8080/swagger-ui**

---

#### Running Without Docker

If you prefer to run locally without Docker you may run `./gradlew bootRun`

---

## Running Tests

```bash
./gradlew test
```

Tests use an **in-memory H2 database** and do not require a live Supabase connection. The test configuration is in `src/test/resources/application.properties`.

---

## API Overview

All `/api/**` endpoints require a valid Supabase JWT in the `Authorization: Bearer <token>` header.

| Tag | Base Path | Description |
|-----|-----------|-------------|
| Auth | `GET /api/me` | Returns current user info from the JWT |
| Events | `/api/events` | Create, retrieve, and delete calendar events |
| Event Tags | `/api/events/{id}/tags` | Assign and remove tags on events |
| Tags | `/api/tags` | Create and manage personal event tags |
| Todos | `/api/todos` | Manage daily to-do items |
| Calendars | `/api/calendars` | Create, retrieve, and delete calendars |
| Users | `/api/users` | Create, retrieve, and delete user records |
| Admin | `/api/admin/users` | Admin-only user management |

Both events and todos support optional **date filtering** via query parameter:

---

## Allowed Origins (CORS)

The backend accepts requests from:
- `http://localhost:5173` (local frontend dev server)
- `https://frontend-cwvc.onrender.com` (deployed frontend)
