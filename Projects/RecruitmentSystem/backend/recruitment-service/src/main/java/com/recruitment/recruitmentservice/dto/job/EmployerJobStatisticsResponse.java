package com.recruitment.recruitmentservice.dto.job;

public record EmployerJobStatisticsResponse(
        long total,
        long published,
        long draft,
        long closed
) {
}
