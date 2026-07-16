package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.CreateEducationRequest;
import com.recruitment.user.dto.request.UpdateEducationRequest;
import com.recruitment.user.dto.response.EducationResponse;
import com.recruitment.user.entity.Education;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EducationMapper {

    Education toEntity(CreateEducationRequest request);

    EducationResponse toResponse(Education entity);

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
            UpdateEducationRequest request,
            @MappingTarget Education entity
    );

}