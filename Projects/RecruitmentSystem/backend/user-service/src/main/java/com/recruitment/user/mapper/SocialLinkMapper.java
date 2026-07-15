package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.CreateSocialLinkRequest;
import com.recruitment.user.dto.request.UpdateSocialLinkRequest;
import com.recruitment.user.dto.response.SocialLinkResponse;
import com.recruitment.user.entity.SocialLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SocialLinkMapper {

    SocialLink toEntity(CreateSocialLinkRequest request);

    SocialLinkResponse toResponse(SocialLink entity);

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
            UpdateSocialLinkRequest request,
            @MappingTarget SocialLink entity
    );

}