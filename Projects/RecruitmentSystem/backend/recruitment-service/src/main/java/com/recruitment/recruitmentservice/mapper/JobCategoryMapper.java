package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.category.CreateJobCategoryRequest;
import com.recruitment.recruitmentservice.dto.category.JobCategoryResponse;
import com.recruitment.recruitmentservice.dto.category.UpdateJobCategoryRequest;
import com.recruitment.recruitmentservice.entity.JobCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = RecruitmentMapperConfig.class)
public interface JobCategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    JobCategoryResponse toResponse(JobCategory entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobCategory toEntity(CreateJobCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateJobCategoryRequest request,
                      @MappingTarget JobCategory entity);

}
