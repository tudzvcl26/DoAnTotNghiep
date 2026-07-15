package com.recruitment.user.mapper;

import com.recruitment.user.dto.response.SkillResponse;
import com.recruitment.user.entity.UserSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.displayName")
    SkillResponse toResponse(UserSkill entity);

}