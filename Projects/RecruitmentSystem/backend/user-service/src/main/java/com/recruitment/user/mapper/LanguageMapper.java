package com.recruitment.user.mapper;

import com.recruitment.user.dto.response.LanguageResponse;
import com.recruitment.user.entity.UserLanguage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    @Mapping(target = "languageId", source = "language.id")
    @Mapping(target = "languageCode", source = "language.languageCode")
    @Mapping(target = "displayName", source = "language.displayName")
    LanguageResponse toResponse(UserLanguage entity);

}