# Sprint 3.1 Infrastructure Report

**Project:** RecruitmentSystem  
**Date:** 2026-07-12  
**Status:** COMPLETE

## Changes Made

- Completed the Docker Compose 3.9 infrastructure definition for PostgreSQL 17, Redis 8, RabbitMQ 4 Management, and MinIO.
- Added a dedicated `recruitment-network` bridge network and persistent named volumes for every stateful service.
- Added `unless-stopped` restart policies, healthchecks, JSON-file log rotation, explicit port mappings, and separate environment files.
- Added PostgreSQL bootstrap SQL for the `uuid-ossp` and `pgcrypto` extensions, with a Flyway migration boundary documented.
- Improved PowerShell lifecycle scripts with Docker Desktop checks, Compose validation, native-command exit-code handling, coloured output, graceful shutdown, clean reset, orphan cleanup, and clean restart.

## Problems Found and Resolutions

| Problem | Resolution |
|---|---|
| Initial Compose startup exceeded the command timeout while images were being downloaded. | Pulled the required images, then started the stack successfully. |
| Docker Compose v2 reports that the legacy `version` property is obsolete. | Retained `version: "3.9"` because Sprint requirements explicitly require Compose 3.9. This is a non-failing Compose v2 warning. |

## Validation Results

| Check | Result |
|---|---|
| `docker compose -f infrastructure/compose/docker-compose.infrastructure.yml config --quiet` | PASS |
| PostgreSQL `pg_isready` | PASS — accepting connections |
| Redis authenticated `PING` | PASS — `PONG` |
| RabbitMQ diagnostics | PASS — ping succeeded |
| RabbitMQ Management UI (`:15672`) | PASS — HTTP 200 |
| MinIO health API (`:9000/minio/health/live`) | PASS — HTTP 200 |
| MinIO Console (`:9001`) | PASS — HTTP 200 |
| Docker containers | PASS — all four running and healthy |
| `recruitment-network` | PASS — bridge/local |
| Named volumes | PASS — four local volumes present |
| PowerShell `start.ps1` | PASS |
| Environment files | PASS — all referenced by Compose |
| Initialization SQL | PASS — mounted read-only into PostgreSQL bootstrap directory |

## Active Infrastructure

| Service | Container | Status |
|---|---|---|
| PostgreSQL 17 | `recruitment-postgres` | healthy |
| Redis 8 | `recruitment-redis` | healthy |
| RabbitMQ 4 Management | `recruitment-rabbitmq` | healthy |
| MinIO | `recruitment-minio` | healthy |

## Remaining Risks

- Development credentials are intentionally stored in local `.env` files. Replace them with a secret manager or deployment-injected secrets before any shared or production environment.
- `minio/minio:latest` can change over time. Pin a tested release digest before production deployment.
- Host port mappings are appropriate for local development; production should restrict exposure behind private networking, TLS, and an ingress or reverse proxy.

## Recommendations Before Sprint 4

1. Add Flyway to the Spring Boot backend and store versioned database migrations in the application migration module.
2. Add application health dependencies using Compose `depends_on` with `service_healthy` when backend services are introduced.
3. Introduce environment-specific secrets, image tags/digests, backup procedures, and observability before deployment beyond local development.
4. Add CI validation for `docker compose config` and a smoke-test job for service health endpoints.
