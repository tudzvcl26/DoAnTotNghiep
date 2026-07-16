package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.CreateExperienceRequest;
import com.recruitment.user.dto.request.UpdateExperienceRequest;
import com.recruitment.user.dto.response.ExperienceResponse;
import com.recruitment.user.entity.Experience;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExperienceMapper {

    Experience toEntity(CreateExperienceRequest request);

    ExperienceResponse toResponse(Experience entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(
            UpdateExperienceRequest request,
            @MappingTarget Experience entity
    );

}