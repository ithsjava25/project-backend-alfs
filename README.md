# ALFS Whistleblower Ticket System

ALFS is a secure case management system for handling whistleblower reports, built with Spring Boot.

## Features
 - Anonymous and authenticated ticket submission
 - Ticket lifecycle management (assignment, status, comments)
 - Internal comments for private admin/investigator communication
 - Audit logging for all key actions
 - Demo data available for quick local testing

## Security
- Secure password hashing with BCrypt
- Role-based access control (RBAC)
- Owner-based access restrictions
- Token-based access for anonymous users
- Server-side rendering with JTE ensures automatic HTML escaping to prevent XSS
- Secure file uploads with MinIO storage

## Feature Details
### Anonymous Ticket Submission
Anonymous (unauthenticated) users can submit tickets without creating an account. A unique secure token is generated on submission — anyone with the token can view and comment on that specific ticket without logging in.

### Ticket Lifecycle Management
Tickets follow a structured lifecycle to ensure proper handling from submission to resolution.

#### Statuses
- **OPEN**: Default state for new submissions.
- **IN_PROGRESS**: The ticket is currently being handled by an investigator.
- **RESOLVED**: The investigation is complete, and a resolution has been reached.
- **CLOSED**: The case is finalized and no further actions are expected.

#### Assignment Flow
1.  **Submission**: A ticket is created in the **OPEN** state.
2.  **Triage**: An **ADMIN** reviews the ticket and assigns it to an **INVESTIGATOR**.
3.  **Investigation**: The assigned investigator updates the status to **IN_PROGRESS** and interacts with the reporter.
4.  **Resolution**: Once finished, the investigator or admin moves the ticket to **RESOLVED** or **CLOSED**.

#### Communication
The system supports two types of comments:
- **Public Comments**: Visible to everyone with access to the ticket (Reporters, Investigators, Admins). Used for follow-up questions and providing updates.
- **Internal Notes**: Visible only to **INVESTIGATORS** and **ADMINS**. These are kept separate from public comments to ensure internal coordination stays private, making it easier to discuss case handling and assignments without exposing sensitive details.

### Audit Logging
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

## Quick start
1. Make sure you have Java 25 and Maven installed.
2. Enable demo data by adding the following to your local `src/main/resources/application.properties`:

```properties
spring.profiles.active=demo
```

*Alternatively, run with the profile directly via command line (see step 3).*

3. Start the application:
   ```bash
   # Using the property file
   ./mvnw spring-boot:run

   # OR: directly enabling the demo profile via command line
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
   ```
4. Open the app in your browser at:
   ```text
   http://localhost:8080
   ```
5. Log in at `http://localhost:8080/login` using the default admin credentials:
   ```text
   username: admin
   password: admin
   ```

> **Note:** To test file uploads and downloads, you'll need a local MinIO instance running. See the [Local MinIO setup](#local-minio-setup-dev-and-testing-file-upload-download) section below.

## Typical User Flow
1. **As a Whistleblower (Anonymous)**: Visit `/tickets/create` to submit a report. Save the provided token to follow up later.
2. **As an Authenticated Reporter**: Login at `/login`, then go to `/tickets/my` to see and track your submitted reports.
3. **As an Admin**: Login at `/login` (user: `admin`), then go to `/admin/tickets` to see all reports and assign them to investigators.
4. **As an Investigator**: Login at `/login`, then check `/tickets/assigned` for cases assigned to you.

## Project Structure
- `controllers` — web endpoints
- `services` — business logic
- `repositories` — database access
- `entities` — JPA models
- `dto` and `mapper` — request/response mapping
- `security` — authentication and JWT handling

## Architecture

The application follows a layered architecture with server-side rendering:

```text
Browser (JTE) → Controller → Service → Repository → Database
```
This layered architecture separates concerns:
- Controllers handle HTTP and validation
- Services enforce business rules and security
- Repositories abstract persistence

Security is enforced at both controller and service levels to prevent bypassing access rules.

- **JTE Templates**: Generate the HTML on the server.
- **Controllers**: Handle HTTP requests, form submissions, and redirects.
- **Services**: Implement business logic, security checks, and audit logging.
- **Repositories**: Standard Spring Data JPA repositories for H2.
- **Storage Service**: Abstracts interaction with MinIO/S3 for file attachments.

## CI/CD
The project uses GitHub Actions for:
- CI on push and pull requests
- CD builds that publish a JAR artifact on `main`

## Role-Based Access Control (RBAC)

The system implements a multi-layered security model to protect whistleblower reports and ensure that only authorized personnel can access sensitive information.

### Roles and Permissions

| Role | Purpose | Permissions |
| --- | --- | --- |
| **REPORTER** | Standard whistleblower user. | Create tickets, view their own tickets, and add comments. |
| **INVESTIGATOR**| Internal staff handling cases. | View assigned tickets, update ticket status, and add comments (public & internal). |
| **ADMIN** | System administrator. | Full access to all tickets, assign investigators to tickets, and system management. |

### Access Control Mechanisms

1.  **Endpoint Protection**: Configured in `SecurityConfig.java`, defining which URL patterns are public (e.g., login, signup, anonymous ticket creation) and which require authentication.
2.  **Method Security**: Using `@PreAuthorize` annotations on controller methods to enforce role requirements.
3.  **Owner-Based Access**: Tickets created by a `REPORTER` are only accessible to that specific user, the assigned `INVESTIGATOR`, and all `ADMIN` users.
4.  **Token-Based Access**: For anonymous reports, a unique secure token is generated. Anyone with the token can view and comment on that specific ticket without needing an account.
5.  **Role Refresh**: Roles are loaded from the database on every request (via JWT filter) to ensure permission changes take effect immediately.

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
  - `storage.s3.accessKey=minio`
  - `storage.s3.secretKey=minio123`
  - `storage.s3.bucket=alfs-attachments`
  - `spring.servlet.multipart.max-file-size=50MB`

4) Test file upload (POST)
- Start the Spring Boot app (default port 8080).
- Use Postman or curl:
  ```bash
  curl -X POST "http://localhost:8080/api/files/upload" \
       -F ticketId=1 \
       -F file=@"/path/to/your/test.pdf"
  ```
- Since `AttachmentController` is a `@Controller`, it will return a redirect (302) to the ticket view page.
- Check MinIO Console → your bucket → object is present.

5) Test file download (GET)
- Use the attachment ID (e.g., 1) to download the file:
  ```bash
  curl -v -o downloaded.pdf "http://localhost:8080/api/files/1/download"
  ```
- The file should be downloaded as `downloaded.pdf`.

### Notes
- A ticket with the provided ticketId must exist in the database before uploading.