# Sprint A.2 — Recruitment Service Code Cleanup Report

## 1. Files đã sửa

- `src/main/java/com/recruitment/recruitmentservice/security/JwtAuthenticationFilter.java`
- `src/main/java/com/recruitment/recruitmentservice/repository/JobRepository.java`
- `src/main/java/com/recruitment/recruitmentservice/repository/JobCategoryRepository.java`
- `src/main/java/com/recruitment/recruitmentservice/repository/SkillRepository.java`
- `src/main/java/com/recruitment/recruitmentservice/repository/BenefitRepository.java`
- `src/main/java/com/recruitment/recruitmentservice/service/impl/JobServiceImpl.java`
- `src/main/java/com/recruitment/recruitmentservice/service/impl/JobCategoryServiceImpl.java`
- `src/main/java/com/recruitment/recruitmentservice/service/impl/SkillServiceImpl.java`
- `src/main/java/com/recruitment/recruitmentservice/service/impl/BenefitServiceImpl.java`
- `src/main/java/com/recruitment/recruitmentservice/controller/JobController.java`

Không đổi package, URL API, DTO, Entity, kiến trúc, Authorization, Auth Service, hoặc thêm endpoint/tính năng mới.

## 2. Những lỗi đã phát hiện

1. `JwtAuthenticationFilter` còn `printStackTrace()` và nhiều `System.out.println()`, có thể làm lộ thông tin lỗi JWT và không theo chuẩn logging.
2. `Job` đã soft delete bằng `active=false`, nhưng `getById`, list, và search vẫn đọc cả bản ghi inactive.
3. `JobCategory`, `Skill`, và `Benefit` có cờ `active` nhưng Delete API vẫn gọi hard delete; list/search/get cũng chưa lọc inactive.
4. Repository thiếu các query `active=true` tương ứng cho các luồng đọc dữ liệu.
5. `JobController.delete` khai báo HTTP 204 nhưng trả về `ApiResponse` body, trái với HTTP semantics.
6. Một số repository method cũ không còn được gọi sau khi chuyển sang active-filtered query.

## 3. Những lỗi đã sửa

### Debug code cleanup

- Thay toàn bộ debug console/stack trace trong `JwtAuthenticationFilter` bằng `@Slf4j` và log `WARN` chỉ chứa tên lớp exception, không chứa token hay raw exception message.
- Rà soát toàn bộ source: không còn `System.out.println`, `System.err.println`, `printStackTrace`, `TODO`, hoặc `FIXME`.

### Soft delete audit

| Entity | Delete hiện tại | Get/List/Search |
|---|---|---|
| Job | Giữ `active=false` | Chỉ trả dữ liệu `active=true` |
| JobCategory | Chuyển hard delete thành `active=false` | Chỉ trả dữ liệu `active=true`; giữ `EntityGraph(parent)` |
| Skill | Chuyển hard delete thành `active=false` | Chỉ trả dữ liệu `active=true` |
| Benefit | Chuyển hard delete thành `active=false` | Chỉ trả dữ liệu `active=true` |

- Bổ sung các repository method active-filtered cho `findById`, pageable list, và name/title search.
- `JobService` chỉ nhận `JobCategory` active khi create/update job, tránh gán job vào category đã bị soft delete.
- Các bản ghi đã soft delete được trả về như “not found” cho các API get/update/delete sử dụng helper hiện có.

### Repository and code-quality cleanup

- Xóa các derived repository query cũ không còn được sử dụng: unfiltered `findAll`/search và các Job query không có caller.
- Không thay đổi business rule hiện có của `JobCategory.delete`: category có child hoặc đang được job tham chiếu vẫn bị chặn trước khi deactivate.

### REST cleanup

- Đổi `JobController.delete` từ `204 No Content` sang `200 OK` vì API hiện trả `ApiResponse` body. URL và response body không đổi.

## 4. Những vấn đề còn lại

- Authorization, role checking, company ownership, và `@PreAuthorize` chưa được thêm theo đúng yêu cầu: đây là Sprint B.
- `JobController` vẫn dùng Spring `Pageable` trực tiếp; việc allow-list sort field/cap page size toàn module cần được quyết định ở Sprint kế tiếp để tránh thay đổi contract query hiện tại.
- Không thêm unit/integration test theo phạm vi Sprint A.2. Test CRUD, filtering, invalid JWT, và pagination vẫn cần được bổ sung ở Sprint B/QA.
- JobSkill, JobBenefit, và JobLocation chưa có service/controller vertical slice; không thay đổi trong Sprint này.
- Các vấn đề ngoài scope cleanup như optimistic locking, cross-field validation, PostgreSQL migration integration test, và secret rotation vẫn cần được xử lý ở sprint phù hợp.

## 5. Đánh giá mức độ hoàn thiện Recruitment Service

**Khoảng 62%.** Các CRUD hiện có đã sạch debug code hơn và soft-delete behaviour được nhất quán cho Job, JobCategory, Skill, Benefit. Service chưa thể coi là production-ready đầy đủ do chưa có Authorization, behavioural test coverage, và một số hardening/validation ngoài scope A.2.

## 6. Danh sách công việc còn lại trước Sprint B

1. QA xác nhận soft-delete contract với nghiệp vụ: category/skill/benefit đã inactive không xuất hiện ở client-facing read APIs.
2. Chạy smoke test thủ công cho create, update, delete, get, list, và search của bốn resource hiện có.
3. Chuẩn bị quy tắc role/company ownership và matrix quyền cho Sprint B; chưa triển khai trong service.
4. Chuẩn bị test plan cho pagination/sort contract của `JobController` trước khi thay đổi validation ở sprint sau.
5. Xác nhận chiến lược test PostgreSQL/Flyway cho CI ở sprint QA/production hardening.

## Verification

Lệnh cuối cùng đã chạy:

```text
mvn clean compile
```

Kết quả: **BUILD SUCCESS**.
