package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.UpdateProfileRequest;
import com.recruitment.user.dto.response.ProfileResponse;
import com.recruitment.user.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileResponse toResponse(Profile profile);
    void updateEntity(UpdateProfileRequest request, @MappingTarget Profile profile);
}
