package com.recruitment.company.specification;

import com.recruitment.company.entity.Company;
import com.recruitment.company.enums.CompanyStatus;
import com.recruitment.company.enums.VerificationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CompanySpecification {

    private CompanySpecification() {
    }

    public static Specification<Company> search(
            String keyword,
            CompanyStatus status,
            VerificationStatus verificationStatus,
            UUID ownerId
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {

                String pattern = "%" + keyword.trim().toLowerCase() + "%";

                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("name")), pattern),
                                cb.like(cb.lower(root.get("slug")), pattern),
                                cb.like(cb.lower(root.get("email")), pattern),
                                cb.like(cb.lower(root.get("website")), pattern),
                                cb.like(cb.lower(root.get("phone")), pattern)
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            if (verificationStatus != null) {
                predicates.add(
                        cb.equal(
                                root.get("verificationStatus"),
                                verificationStatus
                        )
                );
            }

            if (ownerId != null) {
                predicates.add(
                        cb.equal(root.get("ownerId"), ownerId)
                );
            }

            query.orderBy(
                    cb.desc(root.get("createdAt"))
            );

            return cb.and(predicates.toArray(new Predicate[0]));

        };

    }

}