package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.UpdateProfileRequest;
import com.recruitment.user.dto.response.ProfileResponse;
import com.recruitment.user.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileResponse toResponse(Profile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "profileStatus", ignore = true)
    @Mapping(target = "completionScore", ignore = true)
    @Mapping(target = "completionCalculatedAt", ignore = true)
    @Mapping(target = "careerObjective", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "userSkills", ignore = true)
    @Mapping(target = "userLanguages", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "socialLinks", ignore = true)
    @Mapping(target = "candidatePreference", ignore = true)
    @Mapping(target = "assets", ignore = true)
    @Mapping(target = "profileVisibility", ignore = true)
    void updateEntity(
            UpdateProfileRequest request,
            @MappingTarget Profile profile
    );

}