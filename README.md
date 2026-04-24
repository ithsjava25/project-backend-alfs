# ALFS Whistleblower Ticket System

ALFS is a secure case management system for handling whistleblower reports, built with Spring Boot.

## Features
- Anonymous and authenticated ticket submission
- Ticket assignment, status updates, and comments
- Secure file attachments with MinIO storage
- Role-based access control
- Internal notes for private admin/investigator communication
- Audit logging for all key actions
- Server-side rendered UI with JTE templates
- Demo data for quick local testing

## Internal Messaging
The system supports internal notes for communication between admins and investigators.  
These messages are kept separate from public ticket comments so internal coordination stays private.  
This makes it easier to discuss case handling, assignments, and follow-up work without exposing sensitive details.

## Audit Logging
The application keeps an audit trail of important actions so changes can be reviewed later.  
It records details such as the action type, affected field, previous value, new value, and timestamp.  
This helps track ticket updates, assignment changes, status changes, comments, and other key workflow events.

## Tech Stack
- Java 25
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- Jakarta EE APIs
- Lombok
- JTE templates
- H2 for local development
- MinIO for file storage
- JUnit 5, Mockito, and Spring test support

## How to Run
1. Make sure you have Java 25 and Maven installed.
2. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Open the app in your browser at:
   ```text
   http://localhost:8080
   ```

## Local Development Notes
- Demo data is available for quick local testing.
- H2 console is available for local database inspection.
- MinIO is used for attachment storage in development.

## Project Structure
- `controllers` — web endpoints
- `services` — business logic
- `repositories` — database access
- `entities` — JPA models
- `dto` and `mapper` — request/response mapping
- `security` — authentication and JWT handling

## CI/CD
The project uses GitHub Actions for:
- CI on push and pull requests
- CD builds that publish a JAR artifact on `main`

## Local MinIO setup (dev) and testing file upload/download

1) Start MinIO locally (Docker)
- Windows PowerShell example:
  - Create a data folder (optional): `mkdir C:\minio\data`
  - Run MinIO:
    ```powershell
    docker run --name minio -p 9000:9000 -p 9001:9001 `
      -e MINIO_ROOT_USER=minioadmin -e MINIO_ROOT_PASSWORD=minioadmin `
      -v C:\minio\data:/data `
      -d quay.io/minio/minio server /data --console-address ":9001"
    ```

2) Create the bucket
- Open MinIO Console: http://localhost:9001 (user: `minioadmin`, pass: `minioadmin`).
- Go to Buckets → Create bucket → name it: `alfs-attachments`.

3) Verify application configuration (already set for local dev)
- See `src/main/resources/application.properties`:
  - `storage.s3.endpoint=http://localhost:9000`
  - `storage.s3.accessKey=minioadmin`
  - `storage.s3.secretKey=minioadmin`
  - `storage.s3.bucket=alfs-attachments`
  - `spring.servlet.multipart.max-file-size=50MB`

4) Test file upload (POST)
- Start the Spring Boot app (default port 8080).
- Use Postman or curl:
  ```bash
  curl -X POST "http://localhost:8080/api/files/upload" \
       -F ticketId=1 \
       -F file=@"C:/path/to/your/test.pdf"
  ```
- Expected JSON response example:
  ```json
  {
    "id": 42,
    "ticketId": 1,
    "fileName": "test.pdf",
    "s3Key": "<generated-uuid>/test.pdf",
    "uploadedAt": "2026-04-02T12:34:56"
  }
  ```
- Check MinIO Console → your bucket → object is present.

5) Test file download (GET)
- Take the `id` from the upload response above and request:
  ```bash
  curl -v -o downloaded.pdf "http://localhost:8080/api/files/42/download"
  ```
- The file should be downloaded as `downloaded.pdf`.

## Demo Data

The application includes a demo data seeder that populates the database with realistic test data on startup.

To enable it, add the following to your local `application.properties` (do not commit this):

```properties
spring.profiles.active=demo
```
The seeder will only run if the admin user does not exist, so it is safe to leave the profile active during development.

### What gets seeded:

- 5 users: admin, investigator1, investigator2, reporter1, reporter2
- 4 tickets in various states, including one anonymous submission
- Comments and attachments spread across the tickets
- A full audit trail recording every action

### Demo credentials:

> **Note:** These credentials are for local development only. Never use username-as-password patterns in production.

| Username | Password | Role |
| --- | --- | --- |
| admin | admin | ADMIN |
| investigator1 | investigator1 | INVESTIGATOR |
| investigator2 | investigator2 | INVESTIGATOR |
| reporter1 | reporter1 | REPORTER |
| reporter2 | reporter2 | REPORTER |

Once running, the H2 database console is available at `/h2-console` using the credentials in `application.properties`.

-------

## 🔐 Role-Based Access Control (RBAC)

The system implements a multi-layered security model to protect whistleblower reports and ensure that only authorized personnel can access sensitive information.

### Roles and Permissions

| Role | Purpose | Permissions |
| --- | --- | --- |
| **REPORTER** | Standard whistleblower user. | Create tickets, view their own tickets, and add comments. |
| **INVESTIGATOR**| Internal staff handling cases. | View assigned tickets, update ticket status, and add comments. |
| **ADMIN** | System administrator. | Full access to all tickets, assign investigators to tickets, and system management. |

#### Anonymous Access
Anonymous (unauthenticated) users can submit tickets without an account. A unique secure token is generated on submission — anyone with the token can view and comment on that specific ticket without logging in.

### Access Control Mechanisms

1.  **Endpoint Protection**: Configured in `SecurityConfig.java`, defining which URL patterns are public (e.g., login, signup, anonymous ticket creation) and which require authentication.
2.  **Method Security**: Using `@PreAuthorize` annotations on controller methods to enforce role requirements at the service and controller levels.
3.  **Owner-Based Access**: Tickets created by a `REPORTER` are only accessible to that specific user, the assigned `INVESTIGATOR`, and all `ADMIN` users.
4.  **Token-Based Access**: For anonymous reports, a unique secure token is generated. Anyone with the token can view and comment on that specific ticket without needing an account.

### Database Entities

-   **User**: Stores credentials and the assigned `Role`.
-   **Role**: An enumeration (`REPORTER`, `INVESTIGATOR`, `ADMIN`) that defines the user's authority level.

-------

## 🏗️ Architecture

The application follows a layered architecture:

```text
Controller → Service → Repository → Database
```

- Controllers handle HTTP requests and responses
- Services contain business logic
- Repositories handle data access

-------~~