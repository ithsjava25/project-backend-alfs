# ALFS Whistleblower Ticket System

ALFS is a secure case management system for handling whistleblower reports, built with Spring Boot.

## Features
- **Anonymous & Authenticated Reporting**: Submit reports without an account using secure tokens or as a registered user.
- **Ticket Lifecycle Management**: Full workflow support including assignment, status transitions, and public/internal comments.
- **Audit Logging**: Comprehensive trail of all security-sensitive actions and ticket modifications.
- **Server-Side Rendering**: Fast, secure UI built with JTE templates.
- **Secure Storage**: File attachments managed via MinIO/S3-compatible storage.
- **Secure Access Model**: RBAC, ownership validation, and token-based access control.

## Security
- **RBAC**: Multi-layered permissions for Reporters, Investigators, and Admins.
- **Owner-Based Access**: Strict isolation of ticket data based on reporter and assigned investigator.
- **Secure Token Access**: Unique cryptographic tokens for anonymous ticket follow-ups.
- **Data Protection**: Secure password hashing with BCrypt and CSRF protection for all state-changing requests.
- **XSS Prevention**: Automatic HTML escaping via server-side JTE rendering.
- **JWT Authentication**: Supports both browser cookies and API headers with database-backed role verification.

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
The application maintains a comprehensive audit trail of security-sensitive actions and ticket modifications.

**What is logged:**
- Ticket creation and status changes.
- Investigator assignments and reassignments.
- New public comments and internal notes.
- File attachment uploads.

Each log entry records the action type, affected field, previous and new values, the acting user, and a precise timestamp. This provides accountability and helps meet compliance requirements for whistleblower systems.

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
   *The landing page provides quick links to submit a report or log in.*

5. Log in at `http://localhost:8080/login` (or via the UI) using the default admin credentials:
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

## Design Decisions

- Server-side rendering (JTE) was chosen over an SPA approach to reduce complexity
  and minimize client-side security concerns (e.g., XSS, token handling)
- Token-based access allows anonymous reporting without account creation
- MinIO enables S3-compatible storage without requiring a cloud provider

## Project Structure
- `controllers` — web endpoints (UI and API)
- `services` — business logic
- `repositories` — database access
- `entities` — JPA models
- `dto` and `mapper` — request/response mapping
- `security` — Spring Security configuration, authentication, and access control

## Architecture

The application follows a layered architecture with server-side rendering:

```text
Browser (JTE) → Controller → Service → Repository → Database
```
This layered architecture separates concerns:
- **Controllers**: Handle HTTP requests, input validation, and view redirects.
- **Services**: Orchestrate business logic, enforce security rules, and trigger audit logging.
- **Repositories**: Abstract database persistence using Spring Data JPA.
- **Storage Service**: Encapsulates interaction with MinIO/S3 for attachment handling.
- **Audit Service**: Records a detailed trail of modifications for compliance and tracking.

Security is enforced at both controller (URL-based) and service levels (method-based) to ensure defense-in-depth.

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
2.  **Method Security**: Using `@PreAuthorize` annotations on controller methods to enforce role requirements (e.g., restricting status updates to Admins and Investigators).
3.  **Owner-Based Access**: Tickets created by a `REPORTER` are only accessible to that specific user, the assigned `INVESTIGATOR`, and all `ADMIN` users.
4.  **Token-Based Access**: For anonymous reports, a unique secure token is generated. Anyone with the token can view and comment on that specific ticket without needing an account.

## JWT Authentication

The application uses JSON Web Tokens (JWT) for stateless authentication, supporting both browser and API-based interactions.
Even though the UI is server-rendered, the system remains stateless by storing the JWT in a cookie.

### Key Features
- **Dual-Source Lookup**: The system identifies the user via the `JWT` cookie (for browser/UI) or the `Authorization: Bearer <token>` header (for API clients).
- **Database-Backed Roles**: Roles are reloaded from the database on every request. This ensures that permission changes (e.g., revoking admin rights) take effect immediately, even if the user has a long-lived token.
- **Security Secret**: Local development uses a secret defined in `application.properties`. In production, this must be provided via the `JWT_SECRET` environment variable.
- **API Endpoints**: RESTful authentication is available at `/auth/login` and `/auth/signup` for JSON-based programmatic access.

# 🚀 API Documentation & Testing
## This project uses Swagger UI to provide a visual interface for exploring and testing the API endpoints.

### Accessing Swagger
Once the application is running, you can access the interactive documentation at:

Swagger UI: http://localhost:8080/swagger-ui/index.html

OpenAPI Spec (JSON): http://localhost:8080/v3/api-docs
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

Once running, the H2 database console is available at `/h2-console` using the credentials in `application.properties` (JDBC URL: `jdbc:h2:mem:testdb`).

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