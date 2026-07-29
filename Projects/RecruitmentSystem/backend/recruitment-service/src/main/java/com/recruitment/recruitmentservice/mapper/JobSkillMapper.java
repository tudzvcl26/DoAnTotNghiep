package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.jobskill.CreateJobSkillRequest;
import com.recruitment.recruitmentservice.dto.jobskill.JobSkillResponse;
import com.recruitment.recruitmentservice.dto.jobskill.UpdateJobSkillRequest;
import com.recruitment.recruitmentservice.entity.JobSkill;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = RecruitmentMapperConfig.class)
public interface JobSkillMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    @Mapping(target = "skillSlug", source = "skill.slug")
    JobSkillResponse toResponse(JobSkill entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobSkill toEntity(CreateJobSkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateJobSkillRequest request,
                      @MappingTarget JobSkill entity);

}
