# Company Service Development Guideline

**Status:** Mandatory development standard  
**Applies to:** all source, migrations, REST APIs, tests and runtime configuration introduced for `backend/company-service`.  
**Basis:** deep review of the implemented `auth-service` and `user-service` as of 2026-07-27.  
**Important:** This document is a development standard, not an instruction to modify Auth or User in this review.

---

## 1. Purpose and governing decisions

`company-service` will own the employer/company bounded context: company profile, its organization-facing metadata, and future company-owned child resources. It must not read or write Auth or User tables directly. Identity remains owned by Auth; candidate profile remains owned by User.

This service must preserve the useful conventions already demonstrated in User Service, while avoiding its known security and contract inconsistencies. Where this guideline conflicts with existing code, this guideline wins for all new Company Service code.

### Non-negotiable rules

1. Use Java 21, Spring Boot 3.x, Spring MVC, Spring Security, Jakarta Validation, Spring Data JPA, PostgreSQL, Flyway, Lombok, MapStruct and springdoc in versions aligned with the platform.
2. Own data in a dedicated `company_service` PostgreSQL schema and use a dedicated Flyway history table. Do not create foreign keys to `public` or `user_service`.
3. Every protected request must enforce both authentication and **object-level authorization**. A UUID in the path is never proof of ownership.
4. All externally visible responses, including validation and authentication errors, must use one consistent response contract.
5. All schema changes are immutable, versioned Flyway migrations. Hibernate validates but never generates production DDL.
6. No secret, password, access key or JWT signing material may be committed in Java/YAML/env files.
7. Public controller methods accept/return DTOs only; never return JPA entities.

## 2. Reference conventions observed in the current project

| Concern | Existing pattern | Company Service decision |
|---|---|---|
| Root package | `com.recruitment.auth`, `com.recruitment.user` | Use `com.recruitment.company` |
| Layering | technical packages (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`) | Retain; add explicit `security`, `exception`, `config`, `common` |
| Constructors | Lombok `@RequiredArgsConstructor` on Spring components | Mandatory for dependency injection |
| DTOs | `CreateXRequest`, `UpdateXRequest`, `XResponse` | Retain, consistently formatted and immutable where feasible |
| Entity mapping | UUID, `@Entity`, snake_case table/columns, base entity | Retain enhanced User Service base pattern |
| CRUD | controller → service → repository → mapper → response | Retain, with authorization at service boundary |
| Soft delete | `deleted_at`; repository methods end `AndDeletedAtIsNull` | Retain for business records |
| Security | shared JWT claim parsing in User Service | Retain claim shape temporarily; do not retain missing authorization/revocation safeguards |
| API envelope | `ApiResponse<T>` duplicated in both services | Retain the shape, but implement through a shared versioned common contract rather than a local fork |
| Exceptions | Auth has typed `ErrorCode`; User has broader handlers | Adopt typed error codes plus User’s useful HTTP coverage, but one envelope only |

## 3. Mandatory package structure

```text
com.recruitment.company
├─ CompanyServiceApplication.java
├─ common/
│  ├─ ApiResponse.java                 # shared implementation or a platform dependency
│  └─ PageResponse.java
├─ config/
│  ├─ OpenApiConfig.java
│  ├─ SecurityConfig.java
│  └─ <integration>Properties.java
├─ controller/
│  ├─ CompanyController.java
│  └─ <child-resource>Controller.java
├─ dto/
│  ├─ request/
│  │  ├─ CreateCompanyRequest.java
│  │  └─ UpdateCompanyRequest.java
│  └─ response/
│     ├─ CompanyResponse.java
│     └─ CompanySummaryResponse.java
├─ entity/
│  ├─ BaseEntity.java
│  ├─ Company.java
│  └─ <CompanyEnum>.java
├─ exception/
│  ├─ BusinessException.java
│  ├─ ErrorCode.java
│  ├─ ResourceNotFoundException.java
│  └─ GlobalExceptionHandler.java
├─ mapper/
│  └─ CompanyMapper.java
├─ repository/
│  └─ CompanyRepository.java
├─ security/
│  ├─ CurrentUser.java
│  ├─ JwtAuthenticationFilter.java
│  ├─ JwtAuthenticationEntryPoint.java
│  ├─ JwtProperties.java
│  ├─ JwtService.java
│  └─ AuthorizationService.java
└─ service/
   └─ CompanyService.java
```

Create `client/`, `event/`, `integration/`, `specification/` or `storage/` only when there is real functionality. Do not create empty packages or placeholder services. Configuration and transport clients do not belong in controllers or domain services.

## 4. Naming and formatting rules

### Packages, classes, methods and fields

- Package names are lowercase singular technical areas: `com.recruitment.company.service`; no uppercase, underscores or generic `util` dumping ground.
- Class/interface/enum names use PascalCase and a domain suffix when it communicates the layer: `CompanyService`, `CompanyRepository`, `CompanyMapper`, `CompanyStatus`.
- Methods and fields use lower camelCase. A boolean reads as a predicate (`isVerified`, `hasActiveSubscription`), not as a noun.
- Use singular entity names (`Company`, `CompanyLocation`) and plural table/resource names (`companies`, `company_locations`).
- Do not name an entity `CompanyEntity`, a service `CompanyServiceImpl` when there is no interface, or DTOs `CompanyDTO`.
- CRUD method vocabulary is consistent: `getAll`, `getById`, `create`, `update`, `delete`; use `getByOwnerId`, `findActiveById`, `archive`, `activate` only when the domain action is distinct.

### DTO conventions

- Input types: `CreateCompanyRequest`, `UpdateCompanyRequest`, `SearchCompanyRequest`; output types: `CompanyResponse`, `CompanySummaryResponse`.
- A request contains only client-editable values. Identity, ownership, status transitions, audit fields, persistence IDs and server-calculated fields are never client-writable by default.
- Every update request contains `version` when optimistic concurrency is exposed. The service must verify or otherwise enforce it; merely declaring the field is not sufficient.
- Response DTOs expose only API contract fields. Never expose password hashes, internal storage keys, audit data or JPA relation graphs unless an endpoint explicitly requires them.
- One class per file with normal multi-line formatting. Do not repeat User Service’s one-line DTO formatting.

### Lombok rules

- Spring components: `@RequiredArgsConstructor`; dependencies are `private final`.
- JPA entities: `@Getter`, `@Setter`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`, matching the existing base-entity inheritance pattern.
- Response DTOs: `@Getter`, `@Builder`, `@AllArgsConstructor`; use a no-arg constructor only when a framework demonstrably needs it.
- Request DTOs: `@Getter`/`@Setter`; do not make them entities or builders by default.
- Do not use `@Data` or Lombok-generated `equals`/`hashCode` on JPA entities. Do not use `@ToString` on entities with relationships.

### MapStruct rules

- Every mapper is an interface under `mapper/`, annotated `@Mapper(componentModel = "spring")`.
- Standard signatures are `toEntity(CreateXRequest)`, `toResponse(X)`, and `updateEntity(UpdateXRequest, @MappingTarget X)`.
- `updateEntity` must explicitly ignore ID, owner/tenant IDs, audit fields, `deletedAt`, lifecycle status unless its endpoint owns that transition, child collections, and `version` unless deliberately mapped as a checked concurrency token.
- Resolve relationships and business defaults in the service, never in the mapper. MapStruct must not call repositories or security context.
- Mapping compilation must be part of the normal Maven build; generated `target/` sources are never edited.

### Comments, JavaDoc and code format

- Code is English: identifiers, API messages, comments and JavaDoc. Avoid encoding-corrupted non-ASCII comments currently visible in a few configuration classes.
- Comment intent, policy, invariants or a non-obvious decision; do not narrate the code line-by-line. Example: explain why a state transition is restricted, not that `save()` saves.
- JavaDoc is required for public extension points, externally consumed contracts and non-obvious security/transactional decisions. It is optional for obvious CRUD methods.
- Use standard 4-space indentation, one statement per line, braces on the same line as the declaration, and a blank line between logical blocks. Keep imports organized automatically by the IDE. Do not retain the 1–2 space inconsistent indentation found in some User Service classes.

## 5. Base components

### 5.1 BaseEntity

Company Service must start from the richer User Service model, not Auth’s smaller base:

| Field | Rule |
|---|---|
| `id` | UUID, generated by persistence, immutable after creation |
| `createdAt`, `updatedAt` | database/application timestamps through Hibernate creation/update timestamp annotations |
| `deletedAt` | set for logical deletion of business-owned records; null means active |
| `createdBy`, `updatedBy`, `deletedBy` | UUID actor references only, without cross-schema FK |
| `version` | `@Version` optimistic lock field; its contract is applied consistently on update/delete |

`BaseEntity` is `@MappedSuperclass` and must not carry domain behavior. Auditing fields must be populated deliberately through a standard auditing mechanism or explicitly documented service behavior; leaving them permanently null is not a completed audit implementation.

### 5.2 ApiResponse and paging

The mandatory envelope is:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Company retrieved successfully",
  "data": {},
  "timestamp": "2026-07-27T14:00:00",
  "path": "/api/v1/companies/..."
}
```

All non-file endpoints—including errors—use this shape. `data` is omitted when empty. Validation adds an `errors` object within the same contract, for example `{ "errors": { "taxCode": "must not be blank" } }`; it must not switch to a raw `Map` response as User Service currently does.

For list endpoints, use a project-owned `PageResponse<T>` inside `data`, with `content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`. Do not expose Spring `Page` serialization as an API contract.

Until `common-lib` becomes an actual versioned artifact, Company Service may keep a temporary local implementation byte-for-byte contract-compatible with the canonical definition. It must not create a third incompatible envelope.

### 5.3 Exceptions and error codes

Adopt Auth’s typed `ErrorCode`/`BusinessException` idea, extending it with the HTTP coverage of User’s advice.

- Error codes follow `<BOUNDARY>_<NNN>` (`COMPANY_001`) or documented cross-cutting codes (`COMMON_001`, `AUTH_401`).
- `BusinessException` carries the `ErrorCode`; no controller decides error status manually.
- `ResourceNotFoundException` maps to the stable `COMMON_404`/`RESOURCE_NOT_FOUND` convention selected for the platform.
- `GlobalExceptionHandler` handles business errors, bean validation, parameter constraints, unreadable JSON, missing parameters, method-not-allowed, access-denied, authentication, optimistic-lock conflict and unknown errors.
- Unexpected exceptions are logged server-side with stack trace and correlation ID; clients receive a generic message, never SQL or stack details.

Do not copy User Service’s `IllegalStateException → 401` as a general policy: authorization failures must be explicit (`401` unauthenticated, `403` authenticated but forbidden).

### 5.4 Validation, versioning, soft delete and audit

- Validate request DTOs with Jakarta annotations and `@Valid` at controller boundaries. Put cross-field/domain validation in the service and back it with database constraints where data integrity requires it.
- Use database `NOT NULL`, `CHECK`, `UNIQUE` and FK constraints as the final integrity layer; DTO validation is not a substitute.
- Reads of soft-deletable entities must use repository methods that enforce `deletedAt IS NULL`; do not call bare `findById` accidentally. Define explicit admin/restore paths if deleted records must be visible.
- Deletion sets `deletedAt` and `deletedBy`, then saves. Physical deletion requires an explicit retention/purge policy and must not occur through ordinary REST DELETE.
- Optimistic-lock exceptions return a stable conflict error (`409`) with client guidance to refresh. Status codes and messages are tested.

## 6. Security standard

### JWT flow to preserve

The existing Auth-issued access JWT claim model is the compatibility baseline:

| Claim | Meaning |
|---|---|
| `sub` | Auth user email |
| `userId` | stable user UUID |
| `email` | email copy |
| `roles` | roles, e.g. `ADMIN`, `EMPLOYER`, `CANDIDATE` |
| standard `iat`, `exp` | issuance/expiry |

Company Service remains a resource server: it validates a bearer access token, builds `CurrentUser` from claims, and never accepts a refresh token as an API credential. Login/refresh/logout remain Auth responsibilities.

### Security annotations currently in the codebase

Auth declares `@EnableMethodSecurity` on `SecurityConfig`, but its reviewed controllers/services do not use `@PreAuthorize`, `@PostAuthorize`, `@RolesAllowed` or `@Secured`. User Service does not enable method security and likewise has no authorization annotation; it relies solely on `SecurityFilterChain` for the authenticated/not-authenticated boundary. `@SecurityRequirement(name = "Bearer Authentication")` appears on Auth’s protected Swagger operations, while User relies mainly on the global OpenAPI security requirement. `@ConfigurationProperties`, `@Component` and `@Bean` provide the existing security/property wiring.

Company Service must use `@SecurityRequirement` consistently in OpenAPI and may use `@PreAuthorize` only after enabling method security. An annotation is a coarse role gate; it never replaces the service-level object authorization rule below.

### Mandatory filter/configuration pattern

1. `JwtAuthenticationFilter` is a `OncePerRequestFilter`, reads only `Authorization: Bearer <token>`, validates the signature and registered claims, creates an authenticated `JwtAuthenticationToken` with `CurrentUser`, and clears context on failure.
2. `SecurityConfig` is stateless, disables CSRF for token APIs, configures the authentication entry point, permits only health and API documentation, and requires authentication otherwise.
3. `CurrentUser` remains a small serializable principal (`userId`, `email`, `roles`). A `CurrentUserId` helper may be used only after authentication has been established.
4. `JwtProperties` is strongly bound configuration; secrets/keys are injected through deployment secret management, never committed defaults.
5. `JwtService` is responsible for parsing/verifying claims only in a resource service. Token issuance belongs only to Auth.

### Mandatory authorization pattern

Authentication is insufficient. Implement an `AuthorizationService` (or equivalent policy methods) and call it before every owner-scoped mutation/read. It must answer, for example: *may this current user read/update/delete this company?* based on the company owner/administrator relationship and roles.

- Use `@PreAuthorize` when a coarse role policy is clear, and service-level object checks for ownership/tenant membership. Method security must be enabled if annotations are used.
- A route such as `/api/v1/companies/{companyId}` first loads a company through an ownership-aware service/repository query or verifies ownership before returning/modifying it.
- A route with `{userId}` must compare it with `CurrentUser.userId` unless an explicitly authorized role has a documented exception.
- Do not copy User Service’s current `/api/v1/users/{userId}/...` behavior: it authenticates but does not prove the caller may operate on that user’s resource.
- Role strings and authority prefix conventions must match Auth exactly. Existing Auth emits `ADMIN`, `EMPLOYER`, `CANDIDATE`, not `ROLE_ADMIN`; policy implementation must account for that fact.

### Security items to retain vs improve

Retain: stateless filter-chain design, dedicated authentication entry point, OpenAPI bearer scheme, claims-to-principal conversion, one-hour access/seven-day refresh separation, BCrypt in Auth, and non-public business endpoints.

Improve from day one: issuer/audience/algorithm validation; key rotation strategy; secret injection; handling disabled users and token revocation within the required window; explicit authorization; consistent 401/403 responses; an explicit CORS policy; rate limits at an edge; audit logging for sensitive actions. Company Service must not directly share a hard-coded HMAC secret in source. The final trust mechanism (shared symmetric secret vs public-key verification) is a platform decision, but this service must be configuration-ready for it.

## 7. Database and Flyway standard

### Naming and schema ownership

- PostgreSQL schema: `company_service`; configure Hibernate `default_schema`, Flyway `schemas`, `default-schema` and a company-specific history table such as `company_service_flyway_schema_history`.
- Tables are plural snake_case: `companies`, `company_locations`, `company_members`.
- Columns are snake_case: `company_id`, `created_at`, `tax_code`; FK columns are `<referenced_entity>_id`.
- UUID primary keys are named `id`; association tables use a UUID `id` only when they have attributes/audit/version. Pure join tables may use a composite key only where deliberately documented.
- No cross-service foreign keys. Store external identity IDs as UUID values and enforce their lifecycle through APIs/events, not direct database coupling.

### Constraint/index rules

- Declare all business uniqueness in the migration (for example, unique active tax code only if the business policy permits it). The JPA `unique=true` is documentation/validation alignment, not the sole constraint.
- Every FK has a named constraint: `fk_<child>_<parent>`. Define `ON DELETE` behavior intentionally. Soft-deleted parents normally should not rely on DB cascade deletion.
- Name indexes `idx_<table>_<columns>` and unique constraints `uk_<table>_<columns>`. Index FKs and query predicates that are evidenced by API/repository access patterns. Do not index every column.
- Database checks use `chk_<table>_<rule>` and mirror service validation for ranges/state invariants.

### Flyway migration rules

- Location: `src/main/resources/db/migration`; names `V<integer>__<lowercase_snake_case_description>.sql`.
- One logical change per migration. Never edit a migration that may have executed outside a disposable local database; add a new corrective migration instead.
- Migration creates its schema defensively when bootstrapping is required and sets `search_path` consistently.
- SQL creates tables, constraints, indexes and reference seed data. Hibernate runs with `ddl-auto: validate`.
- Enum values persist as `VARCHAR` using `@Enumerated(EnumType.STRING)`, with a deliberate migration length and, where risk warrants it, a check constraint. Never persist enum ordinals.

## 8. REST API standard

### URL, methods and versioning

- All routes begin `/api/v1`.
- Use plural resource nouns: `/api/v1/companies`, `/api/v1/companies/{companyId}/locations`.
- HTTP methods: `GET` read, `POST` create, `PUT` replace/update according to documented semantics, `PATCH` only for genuine partial updates, `DELETE` logical delete/archive. Do not encode CRUD verbs in paths.
- Owner self-service routes may use `/api/v1/companies/me` only if a user can own at most one company; otherwise use an explicit company identifier. Do not introduce ambiguous `/users/{userId}/companies` routes without authorization policy.
- Create returns HTTP `201 Created`, normally with a `Location` header. Successful GET/PUT/PATCH/DELETE return `200` with `ApiResponse`; deletion is not `204` if the envelope is returned.
- Use `UUID` path variables and validated query parameters. Explicitly document default/max page sizes and allowed sort fields; never accept arbitrary sort field paths.

### Controller pattern

Controllers are thin and contain no repository access, security decision logic, transaction orchestration, entity construction or business rule implementation. A typical flow is:

```text
HTTP request → @Valid DTO + path/query values → authorization-aware service → mapper/repository
             ← ApiResponse<Response DTO> + request URI
```

Each controller receives `HttpServletRequest` only to populate the current path in the envelope; request metadata must not become a substitute for a correlation-ID mechanism. Use `@Tag`, `@Operation`, request/response schemas and `@SecurityRequirement` consistently. The global OpenAPI config exposes one bearer scheme named `Bearer Authentication` to remain compatible with existing Swagger UIs.

### Error and validation convention

Every controller uses `@Valid` on request bodies. Bean-validation error keys are field names and values are human-readable stable messages. Cross-field validation produces an explicit business/validation error code rather than a generic `IllegalArgumentException`. Swagger must show validation behavior and security for every protected operation.

## 9. CRUD pattern for Company Service

The repeated User Service pattern is valuable and must be the default:

1. Controller receives an ID/DTO, applies `@Valid`, delegates, and wraps a DTO result.
2. Service is `@Service`, constructor-injected and `@Transactional`; read methods are `@Transactional(readOnly = true)`.
3. Service resolves the parent aggregate/owner, authorizes the current principal, applies domain validation, checks duplicate conditions, maps request to entity or updates an existing entity, persists, and maps the result.
4. Repository extends `JpaRepository<X, UUID>` (and `JpaSpecificationExecutor<X>` only when real filtering requires it) and exposes explicit active-record queries such as `findByIdAndDeletedAtIsNull`.
5. Mapper maps DTOs while ignoring protected persistence and ownership fields.
6. Delete is logical, writes audit data and produces an envelope.

For a company aggregate, use this pattern for company locations, contacts, documents and membership. Do not blindly cascade all child mutations through a loaded JPA aggregate: choose aggregate boundaries intentionally and avoid N+1/large collection loading. Use pagination for collections.

## 10. Auth and User package analysis for maintainers

### Auth Service (`com.recruitment.auth`)

- `config`: authentication provider, BCrypt, stateless security chain and OpenAPI config.
- `controller`: `AuthController` supplies register/login/refresh/logout/me; `HealthController` supplies status.
- `dto.request`/`dto.response`: input validation and auth/current-user response contracts.
- `entity`: `User`, `Role`, `Permission`, `RefreshToken` plus a minimal timestamp/UUID base.
- `repository`: Spring Data lookups by email/name/token.
- `service`: `AuthenticationService` owns transaction-level login/registration/token persistence rules.
- `security`: custom DB user details, JWT generation/parsing and filter.
- `exception`/`common`: typed errors and standard response.

Auth has no generic CRUD controller; its workflow is action-based authentication. Its reusable pattern is the authentication provider + token lifecycle, not a template for Company CRUD.

### User Service (`com.recruitment.user`)

- `config`: security/OpenAPI and MinIO/property binding.
- `controller`: profile and resource CRUD controllers beneath `/api/v1/profiles` and `/api/v1/users/{userId}/...`.
- `dto`: create/update input DTOs and resource/page/profile output DTOs.
- `entity`: profile aggregate, child entities, reference entities and string-persisted enums.
- `repository`: Spring Data JPA pagination, duplicate and soft-delete-aware queries.
- `service`: each domain resource has an explicit CRUD service; profile completion calculation and storage are separate services.
- `mapper`: MapStruct boundary between persistence and API.
- `security`: claims-only JWT resource-server pattern and current-user holder.
- `exception`/`common`: envelope and global handler.

The Company package follows User’s domain CRUD anatomy, not Auth’s workflow-only anatomy.

## 11. Directly reusable assets and patterns

The following are reusable in concept now. Physical reuse should move into a versioned `common-lib`/security starter only after a platform owner defines compatibility and release rules; copying source into Company Service is a temporary measure, not the target.

| Reusable asset/pattern | Source today | Company Service use |
|---|---|---|
| `ApiResponse<T>` contract/factories | Auth and User `common` | Canonical shared envelope after validation-error unification |
| `BaseEntity` rich audit/version model | User `entity.BaseEntity` | Direct conceptual base for all company business entities |
| `GlobalExceptionHandler` coverage | User `exception` | Extend/standardize with Auth typed `ErrorCode` |
| `BusinessException` + `ErrorCode` | Auth `exception` | Direct conceptual error model for business rules |
| JWT claim parsing/principal | User `security.JwtService`, `CurrentUser`, `JwtAuthenticationToken` | Compatibility resource-server layer, hardened as required above |
| `SecurityConfig` / entry point pattern | User `config`, `security` | Stateless resource-server configuration, with mandatory authorization additions |
| OpenAPI bearer configuration | User/Auth `OpenApiConfig` | Service-specific title plus common scheme name |
| MapStruct update ignore-list pattern | User mappers | Mandatory protection of persistence/ownership/audit fields |
| Pagination repository/service flow | User repositories/controllers | Standard list/search endpoints, with `PageResponse` instead of raw Page |
| Soft-delete-aware query names | User repositories | Standard active-record access pattern |
| CRUD service transaction pattern | User `EducationService` and sibling services | Standard create/read/update/delete workflow |
| Property binding and external client config | User `StorageProperties`/`MinioConfig` | Pattern for any real external integration; bind config, inject client |
| Health/OpenAPI endpoints | Both services | Required operability baseline |

Do **not** reuse unchanged: hard-coded secrets/application YAML values, Auth’s minimal BaseEntity, duplicated local common classes as a permanent approach, raw `Page` responses, User’s raw validation error map, User’s unguarded `{userId}` CRUD routes, `AuthServiceProperties` (currently unused and has no corresponding Auth validation endpoint), or `minio/minio:latest` deployment policy.

## 12. Required quality gates before Company Service is accepted

- Maven compilation including Lombok and MapStruct generated code passes.
- Flyway migration runs against a clean PostgreSQL database and Hibernate `validate` passes.
- Unit tests cover domain validation, duplicate rules, state transitions, mapper ignore protections and soft-delete behavior.
- Integration tests cover repository constraints, migrations, controller response/error envelope, pagination and optimistic-lock conflict.
- Security tests prove: anonymous protected requests receive 401; unauthorized roles/owners receive 403; an authenticated user cannot access a different company; documentation/health visibility follows policy.
- API tests verify documented HTTP status, `ApiResponse`/validation envelope and OpenAPI generation.
- No tracked secret is introduced; deployment configuration supplies all sensitive values.
- Logs contain no token, password, refresh token, secret or personally sensitive payload.

## 13. Final developer checklist

Before creating a pull request, the developer confirms:

- [ ] Package, naming, DTO, Lombok and MapStruct conventions in Sections 3–4 are followed.
- [ ] Controller is DTO-only, thin and documented; service owns transactions and business rules.
- [ ] Every ID-based read/write/delete has explicit object-level authorization.
- [ ] Entity extends the required base, maps UUID/string enums correctly and exposes no mutable internal fields through API.
- [ ] Migration owns schema/table/constraint/index changes and is immutable.
- [ ] Repositories filter soft-deleted records by default.
- [ ] Request validation, cross-field checks, error code and database constraints agree.
- [ ] Page/error responses use the canonical contract.
- [ ] Tests satisfy Section 12 and no secrets or generated files are committed.

This guideline deliberately establishes a stricter baseline than the current two services. It preserves their useful conventions and prevents Company Service from multiplying the implementation gaps already identified in the platform review.
