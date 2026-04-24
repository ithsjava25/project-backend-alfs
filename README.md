# The ALFS Whistleblower Ticket System

## A secure case management system built with Spring Boot for handling whistleblower reports.
The system allows anonymous reporting, secure file uploads, role-based access control, and full audit logging.

### Logs should look like this:
```text
action = HANDLER_ASSIGNED
fieldName = assignedHandler
oldValue = null
newValue = userId:5
createdAt = 2026-03-27
```


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

Notes
- No authentication is enforced on these endpoints yet (Week 1 scope).
- Ensure a Ticket with the provided `ticketId` exists in the database before uploading.


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

## 🏗️ Architecture

The application follows a layered architecture:

```text
Controller → Service → Repository → Database
```

- Controllers handle HTTP requests and responses
- Services contain business logic
- Repositories handle data access  

-------