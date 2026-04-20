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


# 🚀 API Documentation & Testing
## This project uses Swagger UI to provide a visual interface for exploring and testing the API endpoints.

### Accessing Swagger
Once the application is running, you can access the interactive documentation at:

Swagger UI: http://localhost:8080/swagger-ui/index.html

OpenAPI Spec (JSON): http://localhost:8080/v3/api-docs