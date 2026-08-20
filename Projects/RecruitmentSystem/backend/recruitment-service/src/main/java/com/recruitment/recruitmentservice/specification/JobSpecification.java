package com.recruitment.recruitmentservice.specification;

import com.recruitment.recruitmentservice.entity.Job;
import com.recruitment.recruitmentservice.dto.job.JobSearchRequest;
import com.recruitment.recruitmentservice.entity.JobLocation;
import com.recruitment.recruitmentservice.entity.JobSkill;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> publicSearch(JobSearchRequest request, boolean admin) {
        return (root, query, builder) -> {
            var predicate = builder.isTrue(root.get("active"));

            if (!admin) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), JobStatus.PUBLISHED));
            }
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.like(builder.lower(root.get("title")), pattern));
            }
            if (request.getCategoryId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            if (request.getCompanyId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("companyId"), request.getCompanyId()));
            }
            if (request.getEmploymentType() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("employmentType"), request.getEmploymentType()));
            }
            if (request.getExperienceLevel() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("experienceLevel"), request.getExperienceLevel()));
            }
            if (request.getRemoteAllowed() != null) {
                predicate = builder.and(predicate, builder.equal(root.get("remoteAllowed"), request.getRemoteAllowed()));
            }
            if (request.getMinSalary() != null) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("salaryMax"), request.getMinSalary()));
            }
            if (request.getMaxSalary() != null) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get("salaryMin"), request.getMaxSalary()));
            }
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                Subquery<UUID> locationJobs = query.subquery(UUID.class);
                var location = locationJobs.from(JobLocation.class);
                String pattern = "%" + request.getLocation().trim().toLowerCase() + "%";
                locationJobs.select(location.get("job").get("id")).where(builder.or(
                        builder.like(builder.lower(location.get("province")), pattern),
                        builder.like(builder.lower(location.get("district")), pattern),
                        builder.like(builder.lower(location.get("address")), pattern)
                ));
                predicate = builder.and(predicate, root.get("id").in(locationJobs));
            }
            if (request.getSkillId() != null) {
                Subquery<UUID> skillJobs = query.subquery(UUID.class);
                var jobSkill = skillJobs.from(JobSkill.class);
                skillJobs.select(jobSkill.get("job").get("id")).where(
                        builder.equal(jobSkill.get("skill").get("id"), request.getSkillId())
                );
                predicate = builder.and(predicate, root.get("id").in(skillJobs));
            }
            return predicate;
        };
    }

    public static Specification<Job> employerJobs(
            Collection<UUID> ownedCompanyIds,
            UUID companyId,
            JobStatus status,
            String keyword,
            boolean admin
    ) {
        return (root, query, builder) -> {
            var predicate = builder.isTrue(root.get("active"));

            if (!admin) {
                if (ownedCompanyIds == null || ownedCompanyIds.isEmpty()) {
                    return builder.disjunction();
                }
                predicate = builder.and(predicate, root.get("companyId").in(ownedCompanyIds));
            }
            if (companyId != null) {
                predicate = builder.and(predicate, builder.equal(root.get("companyId"), companyId));
            }
            if (status != null) {
                predicate = builder.and(predicate, builder.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicate = builder.and(predicate, builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("jobCode")), pattern)
                ));
            }
            return predicate;
        };
    }
}
