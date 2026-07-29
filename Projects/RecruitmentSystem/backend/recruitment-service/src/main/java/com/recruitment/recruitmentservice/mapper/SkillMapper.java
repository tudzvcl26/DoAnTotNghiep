package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.skill.CreateSkillRequest;
import com.recruitment.recruitmentservice.dto.skill.SkillResponse;
import com.recruitment.recruitmentservice.dto.skill.UpdateSkillRequest;
import com.recruitment.recruitmentservice.entity.Skill;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = RecruitmentMapperConfig.class)
public interface SkillMapper {

    SkillResponse toResponse(Skill entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Skill toEntity(CreateSkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateSkillRequest request,
                      @MappingTarget Skill entity);

}
