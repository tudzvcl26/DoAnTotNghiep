package com.recruitment.application.dto.response;

public record EmployerApplicationStatisticsResponse(
        long total,
        long applied,
        long screening,
        long interview,
        long offer,
        long hired,
        long rejected,
        long withdrawn
) {
}
