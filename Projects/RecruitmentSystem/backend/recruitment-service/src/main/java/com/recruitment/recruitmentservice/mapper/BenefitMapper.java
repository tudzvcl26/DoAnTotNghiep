package com.recruitment.recruitmentservice.mapper;

import com.recruitment.recruitmentservice.dto.benefit.BenefitResponse;
import com.recruitment.recruitmentservice.dto.benefit.CreateBenefitRequest;
import com.recruitment.recruitmentservice.dto.benefit.UpdateBenefitRequest;
import com.recruitment.recruitmentservice.entity.Benefit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = RecruitmentMapperConfig.class)
public interface BenefitMapper {

    BenefitResponse toResponse(Benefit entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Benefit toEntity(CreateBenefitRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateBenefitRequest request,
                      @MappingTarget Benefit entity);

}
