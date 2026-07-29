# Sprint A.2 Verification Report

## 1. Checklist

| Hạng mục | Kết quả | Ghi chú |
|---|---|---|
| Debug | PASS | Không còn `System.out.println`, `System.err.println`, `printStackTrace`, `TODO`, `FIXME`. |
| Specification | PASS / N/A | Không có `Specification` hoặc `JpaSpecificationExecutor` trong Recruitment Service, nên không có `JobSpecification` để audit. Soft delete được áp dụng qua derived repository query. |
| Mapper | PASS | Tất cả update mapper ignore `id`, audit fields và null source properties; `JobMapper` đã có null-ignore từ trước. |
| Validation | PASS | Tất cả request body của controller hiện có dùng `@Valid`; request DTO có validation phù hợp với field bắt buộc. |
| Swagger | PASS | Có OpenAPI bearer scheme, `@Tag`, `@Operation`; không phát hiện lỗi rõ ràng cần sửa trong Sprint A.2. |
| Repository | PASS | Read query được dùng cho resource soft-delete đều lọc `active=true`; derived query chết đã được xóa. |
| Service | PASS | Optional handling qua `orElseThrow`; không còn hard delete cho Job/JobCategory/Skill/Benefit. |
| Controller | PASS | URL không đổi; `Job` delete dùng HTTP 200 vì endpoint trả `ApiResponse` body. |
| Security | PASS (scope A.2) | JWT failure dùng SLF4J safe logging và vẫn thiết lập/xóa `SecurityContext` đúng luồng. Không thêm Authorization/role/ownership. |

## 2. Các file đã sửa

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
- `src/main/java/com/recruitment/recruitmentservice/mapper/SkillMapper.java`
- `src/main/java/com/recruitment/recruitmentservice/mapper/BenefitMapper.java`
- `src/main/java/com/recruitment/recruitmentservice/mapper/JobCategoryMapper.java`
- `src/main/java/com/recruitment/recruitmentservice/mapper/JobBenefitMapper.java`
- `src/main/java/com/recruitment/recruitmentservice/mapper/JobSkillMapper.java`
- `src/main/java/com/recruitment/recruitmentservice/mapper/JobLocationMapper.java`

## 3. Các lỗi phát hiện

1. JWT filter còn console/stack-trace debug code.
2. Job soft delete chưa lọc `active=false` tại get/list/search.
3. JobCategory, Skill, Benefit có trường `active` nhưng delete vẫn hard delete; read API cũng có thể trả dữ liệu inactive.
4. Một số repository derived query cũ không còn caller sau khi áp dụng active filter.
5. Job delete khai báo HTTP 204 nhưng trả response body.
6. Sáu update mapper chưa có null-value protection; request null có thể overwrite field entity hiện có.

## 4. Các lỗi đã sửa

- Chuyển JWT failure debug output sang `@Slf4j`, log tên exception, không log token/raw message.
- Dùng `findByIdAndActiveTrue`, `findByActiveTrue`, và active-filtered search cho Job, JobCategory, Skill, Benefit.
- Chuyển delete của JobCategory, Skill, Benefit sang `active=false`; Job đã dùng cùng cơ chế.
- Job create/update chỉ nhận category active.
- Xóa repository query chết/unfiltered không còn được gọi.
- Đồng bộ HTTP delete Job thành 200 để khớp `ApiResponse` body.
- Thêm `NullValuePropertyMappingStrategy.IGNORE` cho các update mapper chưa có, đồng thời giữ ignore `id` và audit fields.

## 5. Kết quả grep

Lệnh đã kiểm tra:

```text
rg -n "System\.(out|err)\.println|printStackTrace|TODO|FIXME" src pom.xml
```

Kết quả: **0 occurrences**.

## 6. Build and Test Verification

Các lệnh đã chạy sau toàn bộ thay đổi:

```text
mvn clean compile
mvn test
```

Kết quả:

- `mvn clean compile` — **BUILD SUCCESS**; compile 80 source files.
- `mvn test` — **BUILD SUCCESS**; 1 test chạy, 0 failures, 0 errors, 0 skipped.

## 7. Đánh giá cuối — Sprint A.2

**Sprint A.2 completed: 100%.**

Phạm vi cleanup đã hoàn thành: debug code, soft-delete filtering, repository cleanup, mapper null protection, REST delete semantic rõ ràng, và verification source. Không thay đổi kiến trúc, URL API, DTO, Entity, hoặc business rule ngoài việc làm cờ `active` hoạt động nhất quán.

## 8. Vấn đề để Sprint B

- Authorization, role checking, company ownership, và `@PreAuthorize` chưa triển khai theo yêu cầu.
- Test CRUD/security/pagination cụ thể chưa được thêm trong Sprint A.2.
- `JobController` vẫn dùng Spring `Pageable` trực tiếp; policy allow-list sort field/cap page size là công việc hardening tiếp theo.

## Conclusion

**READY FOR SPRINT B.** Build và test đã kết thúc thành công.
