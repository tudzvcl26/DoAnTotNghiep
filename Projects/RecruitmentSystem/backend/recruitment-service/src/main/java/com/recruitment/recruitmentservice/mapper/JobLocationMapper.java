package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.location.CreateJobLocationRequest;
import com.recruitment.recruitmentservice.dto.location.JobLocationResponse;
import com.recruitment.recruitmentservice.dto.location.UpdateJobLocationRequest;
import com.recruitment.recruitmentservice.entity.JobLocation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = RecruitmentMapperConfig.class)
public interface JobLocationMapper {

    @Mapping(target = "jobId", source = "job.id")
    JobLocationResponse toResponse(JobLocation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobLocation toEntity(CreateJobLocationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateJobLocationRequest request,
                      @MappingTarget JobLocation entity);

}
