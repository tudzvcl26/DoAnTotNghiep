# Recruitment System API Gateway

Reactive Spring Cloud Gateway for the Recruitment System. Frontend clients use one base URL:

```text
http://localhost:8080
```

The Gateway contains routing, access-token validation, CORS, correlation, request metadata logging, timeouts, and gateway-owned error handling only. Business authorization, roles, ownership, token issuance/refresh, persistence, RabbitMQ, and AI behavior remain in the seven backend services.

## Routes

| Route | Upstream default | Authentication |
|---|---:|---|
| `/api/v1/auth/**` | Auth `8081` | Register/login/refresh public; other endpoints protected |
| `/api/v1/users/**`, `/api/v1/profiles/**` | User `8082` | Protected |
| `/api/v1/companies/**` | Company `8083` | GET public; mutations protected |
| `/api/v1/jobs/**`, `/api/v1/job-categories/**`, `/api/v1/skills/**`, `/api/v1/benefits/**` | Recruitment `8084` | GET public; mutations protected |
| `/api/v1/applications/**` | Application `8085` | Protected |
| `GET /api/v1/jobs/{jobId}/applications` | Application `8085` | Protected; takes precedence over the Recruitment route |
| `/api/v1/notifications/**`, `/api/v1/notification-templates/**`, `/api/v1/admin/notification-delivery-logs/**` | Notification `8086` | Protected |
| `/api/v1/ai/**` | AI `8087` | Protected |

Protected requests require the existing backend access-token format:

```http
Authorization: Bearer <access-token>
```

The Gateway verifies signature, expiration, and `token_type=access` using `JWT_SECRET`, then forwards the original Authorization header. Backend services remain the final authority for role and ownership decisions.

## Public endpoints

- `GET /api/v1/health`
- `GET /actuator/health` and its health component paths
- `OPTIONS /**` for CORS preflight
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- GET endpoints under Companies and the Recruitment job/catalog routes, matching their current backend security configuration

Swagger is not aggregated in this phase. Existing service OpenAPI/Swagger endpoints remain available directly in development and were not changed.

## Environment variables

| Variable | Default |
|---|---|
| `JWT_SECRET` | Required; same Base64 secret as the backend services |
| `AUTH_SERVICE_URL` | `http://localhost:8081` |
| `USER_SERVICE_URL` | `http://localhost:8082` |
| `COMPANY_SERVICE_URL` | `http://localhost:8083` |
| `RECRUITMENT_SERVICE_URL` | `http://localhost:8084` |
| `APPLICATION_SERVICE_URL` | `http://localhost:8085` |
| `NOTIFICATION_SERVICE_URL` | `http://localhost:8086` |
| `AI_SERVICE_URL` | `http://localhost:8087` |
| `GATEWAY_CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` |
| `GATEWAY_CORS_ALLOW_CREDENTIALS` | `false` |
| `GATEWAY_CONNECT_TIMEOUT_MS` | `3000` |
| `GATEWAY_RESPONSE_TIMEOUT_MS` | `120000` |

The 120-second response default allows local Ollama generation to complete. It remains bounded and can be lowered per environment. The Gateway never retries business mutations.

Each request receives or preserves `X-Correlation-ID`; the value is forwarded upstream and returned to the client. Logs contain method, path, status, duration, correlation ID, and route only—never Authorization, JWT, passwords, refresh tokens, request bodies, or resume content.

Gateway-owned errors use this envelope:

```json
{
  "timestamp": "2026-08-09T00:00:00Z",
  "status": 504,
  "code": "GATEWAY_TIMEOUT",
  "message": "The upstream service did not respond in time",
  "path": "/api/v1/ai/example",
  "traceId": "correlation-id"
}
```

Backend responses, including `400`, `401`, `403`, `404`, `409`, and `5xx`, are preserved when an upstream server actually responds. Gateway timeouts return `504`, unavailable upstreams return `503`, and invalid/missing access tokens return `401`.

## Local build and startup

Start the existing infrastructure and seven backend services without changing their ports. From this directory:

```powershell
mvn --batch-mode clean verify
java -jar target/api-gateway-1.0.0.jar
```

The application imports `.env` from the current directory and up to two parent directories, consistent with the backend services.

Health checks:

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
Invoke-RestMethod http://localhost:8080/actuator/health
```

Only the Actuator health endpoint is exposed.

## Docker

Build the standalone image:

```powershell
docker build -t recruitment-api-gateway:local .
```

In a Compose network with service DNS names, configure:

```yaml
api-gateway:
  build: ./backend/api-gateway
  ports:
    - "8080:8080"
  environment:
    JWT_SECRET: ${JWT_SECRET}
    AUTH_SERVICE_URL: http://auth-service:8081
    USER_SERVICE_URL: http://user-service:8082
    COMPANY_SERVICE_URL: http://company-service:8083
    RECRUITMENT_SERVICE_URL: http://recruitment-service:8084
    APPLICATION_SERVICE_URL: http://application-service:8085
    NOTIFICATION_SERVICE_URL: http://notification-service:8086
    AI_SERVICE_URL: http://ai-service:8087
    GATEWAY_CORS_ALLOWED_ORIGINS: ${GATEWAY_CORS_ALLOWED_ORIGINS:-http://localhost:5173}
```

No existing frozen Compose file was modified. Add this service to the intended aggregate Compose deployment when frontend integration begins.
