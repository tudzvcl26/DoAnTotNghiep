package com.recruitment.user.mapper;

import com.recruitment.user.dto.response.ProfileAssetResponse;
import com.recruitment.user.entity.ProfileAsset;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileAssetMapper {

    ProfileAssetResponse toResponse(
            ProfileAsset entity
    );

}