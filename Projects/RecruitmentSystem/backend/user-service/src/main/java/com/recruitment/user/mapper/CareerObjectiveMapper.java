package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.UpdateCareerObjectiveRequest;
import com.recruitment.user.dto.response.CareerObjectiveResponse;
import com.recruitment.user.entity.CareerObjective;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CareerObjectiveMapper {

    CareerObjectiveResponse toResponse(
            CareerObjective entity
    );

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
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
            UpdateCareerObjectiveRequest request,
            @MappingTarget CareerObjective entity
    );

}