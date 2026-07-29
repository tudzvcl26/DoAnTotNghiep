package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.jobbenefit.CreateJobBenefitRequest;
import com.recruitment.recruitmentservice.dto.jobbenefit.JobBenefitResponse;
import com.recruitment.recruitmentservice.dto.jobbenefit.UpdateJobBenefitRequest;
import com.recruitment.recruitmentservice.entity.JobBenefit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = RecruitmentMapperConfig.class)
public interface JobBenefitMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "benefitId", source = "benefit.id")
    @Mapping(target = "benefitName", source = "benefit.name")
    @Mapping(target = "benefitSlug", source = "benefit.slug")
    JobBenefitResponse toResponse(JobBenefit entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "benefit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobBenefit toEntity(CreateJobBenefitRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "benefit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateJobBenefitRequest request,
                      @MappingTarget JobBenefit entity);

}
