# RecruitmentSystem

## Run the complete system with Docker

Requirements: Docker Desktop with Compose v2 and enough disk space for the
application images plus the Qwen model.

1. Copy `.env.example` to `.env`.
2. Set strong values for `JWT_SECRET`, `POSTGRES_PASSWORD`,
   `RABBITMQ_PASSWORD`, `MINIO_ACCESS_KEY`, and `MINIO_SECRET_KEY`. Keep the
   bootstrap switches disabled except during a controlled first-user setup.
3. Build and start the stack:

   ```sh
   docker compose build
   docker compose up -d
   docker compose ps
   ```

The first start pulls `Qwen2.5:3B-Instruct` into the persistent Ollama volume;
later starts reuse it. The web application is available at
`http://localhost:5173` and the API Gateway at `http://localhost:8080`.
Internal backend ports are not published. Infrastructure debug ports are bound
to loopback only.

Use `docker compose down` for a persistent stop/start cycle. Do not add `-v`
unless PostgreSQL, Redis, RabbitMQ, MinIO, and Ollama data is intentionally
disposable. Development values in `.env.example` remain localhost-based;
Compose supplies Docker-network hostnames to containers.
