package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.CreateSkillRequest;
import com.recruitment.user.dto.request.UpdateSkillRequest;
import com.recruitment.user.dto.response.SkillResponse;
import com.recruitment.user.entity.UserSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserSkillMapper {

    UserSkill toEntity(CreateSkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(
            UpdateSkillRequest request,
            @MappingTarget UserSkill entity
    );

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.displayName")
    SkillResponse toResponse(UserSkill entity);

}