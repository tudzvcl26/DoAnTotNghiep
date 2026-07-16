# User Service Test Report

Thời điểm: 2026-07-16 21:29 (Asia/Bangkok)  
Môi trường: `http://localhost:8082`, Swagger UI trên Chrome

## Kết quả đã xác nhận

| Endpoint | Cách test | Kết quả |
|---|---|---|
| `GET /api/v1/health` | Execute trực tiếp trên Swagger UI | PASS — HTTP 200, JSON `success: true`, `status: UP` |

## Lỗi đã sửa

| Lỗi | Nguyên nhân | Sửa đổi | Xác nhận |
|---|---|---|---|
| Health trả HTTP 401 | `SecurityConfig` không permit endpoint `/api/v1/health` | Thêm endpoint vào allowlist `requestMatchers(...).permitAll()` | Compile thành công, restart service, Swagger trả 200 |

## Kiểm tra build và restart

- `mvn -q -DskipTests compile`: PASS.
- User Service đã restart và lắng nghe cổng `8082` (PID mới `17144`).

## Chưa thể hoàn tất trong phiên kiểm thử này

Các endpoint nghiệp vụ yêu cầu JWT. Swagger UI đang không có token Authorization hợp lệ; do đó không thể tạo profile test hoặc thực thi các ca CRUD mà vẫn tuân thủ yêu cầu test qua Swagger UI.

### JWT test-account blocker

- Đã tìm thấy account seed: `admin@recruitment.local` trong `backend/auth-service/src/main/resources/db/migration/V5__seed_admin_account.sql`.
- File chỉ chứa BCrypt hash `$2a$10$lypOytbbiJi5UKVn8zK6p.VyDLVxxPMTCIhCTw3z2A2ThGN7Ydq.u`; không có comment ghi password gốc.
- Đã kiểm tra: toàn bộ migration Auth Service (bao gồm `V4__seed_roles_permissions.sql`, `V5__seed_admin_account.sql`, `V6__seed_permissions.sql`), `backend/auth-service/src/main/resources/application.yaml`, `README.md`, `docs/`, `backend/auth-service/HELP.md` và cấu hình infrastructure. Không có password gốc của account seed.
- Theo yêu cầu, không thực hiện login thử bằng mật khẩu đoán và không tạo account mới.

- Profile: initialize, get/update, activate/deactivate.
- Education, Experience, Skill, Language, Certificate, Career Objective, Candidate Preference, Social Link.
- Profile Asset: upload/download/delete, kiểm tra MinIO và avatar active/deactivate.
- Completion score sau create/delete.
- Đối chiếu database: dữ liệu, FK, soft delete, unique constraint.

## Phát hiện về độ bao phủ API

Swagger hiện không expose endpoint Profile `initialize`, `activate` hoặc `deactivate`; controller chỉ có `GET/PUT/DELETE /api/v1/profiles/me` và `GET /api/v1/profiles`. Các hàm service tồn tại nhưng không có route để kiểm thử qua Swagger.

## Ảnh hưởng và TODO

- Cần cung cấp JWT test hợp lệ (hoặc thực hiện luồng cấp token đã được phê duyệt) trong Swagger Authorize để chạy phần CRUD và kiểm tra database/MinIO.
- Cần expose các route profile còn thiếu nếu chúng là yêu cầu API chính thức; hiện không thể kiểm thử chúng qua Swagger.
- Mức độ hoàn thiện được xác nhận: health và cấu hình public endpoint đã hoàn tất; các luồng nghiệp vụ chưa được xác nhận end-to-end.
