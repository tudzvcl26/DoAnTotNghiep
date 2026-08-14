package com.recruitment.recruitmentservice.specification;

import com.recruitment.recruitmentservice.entity.Job;
import com.recruitment.recruitmentservice.entity.enums.JobStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class JobSpecification {

    private JobSpecification() {
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
