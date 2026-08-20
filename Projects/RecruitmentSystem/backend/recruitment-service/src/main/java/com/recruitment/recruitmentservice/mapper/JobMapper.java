package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.job.CreateJobRequest;
import com.recruitment.recruitmentservice.dto.job.JobResponse;
import com.recruitment.recruitmentservice.dto.job.JobSummaryResponse;
import com.recruitment.recruitmentservice.dto.job.UpdateJobRequest;
import com.recruitment.recruitmentservice.entity.Job;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "jobCode", source = "jobCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "requirements", source = "requirements")
    @Mapping(target = "responsibilities", source = "responsibilities")
    @Mapping(target = "salaryMin", source = "salaryMin")
    @Mapping(target = "salaryMax", source = "salaryMax")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "employmentType", source = "employmentType")
    @Mapping(target = "experienceLevel", source = "experienceLevel")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "applicationDeadline", source = "applicationDeadline")
    @Mapping(target = "remoteAllowed", source = "remoteAllowed")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "companyId", source = "companyId")
    Job toEntity(CreateJobRequest request);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "expiredAt", ignore = true)
    void updateEntity(
            UpdateJobRequest request,
            @MappingTarget Job job
    );

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "location", ignore = true)
    JobResponse toResponse(Job job);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "location", ignore = true)
    JobSummaryResponse toSummaryResponse(Job job);

}
