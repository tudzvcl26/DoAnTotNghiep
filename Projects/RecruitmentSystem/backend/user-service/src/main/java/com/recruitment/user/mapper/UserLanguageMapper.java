package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.CreateLanguageRequest;
import com.recruitment.user.dto.request.UpdateLanguageRequest;
import com.recruitment.user.dto.response.LanguageResponse;
import com.recruitment.user.entity.UserLanguage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserLanguageMapper {

    UserLanguage toEntity(CreateLanguageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(
            UpdateLanguageRequest request,
            @MappingTarget UserLanguage entity
    );

    @Mapping(target = "languageId", source = "language.id")
    @Mapping(target = "languageCode", source = "language.languageCode")
    @Mapping(target = "displayName", source = "language.displayName")
    LanguageResponse toResponse(UserLanguage entity);

}