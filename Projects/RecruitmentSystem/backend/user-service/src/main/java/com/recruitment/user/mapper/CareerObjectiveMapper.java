package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.UpdateCareerObjectiveRequest;
import com.recruitment.user.dto.response.CareerObjectiveResponse;
import com.recruitment.user.entity.CareerObjective;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
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
    void updateEntity(
            UpdateCareerObjectiveRequest request,
            @MappingTarget CareerObjective entity
    );

}