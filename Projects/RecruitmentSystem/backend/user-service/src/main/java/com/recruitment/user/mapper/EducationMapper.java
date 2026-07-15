package com.recruitment.user.mapper;
import com.recruitment.user.dto.request.CreateEducationRequest; import com.recruitment.user.dto.request.UpdateEducationRequest; import com.recruitment.user.dto.response.EducationResponse; import com.recruitment.user.entity.Education; import org.mapstruct.Mapper; import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring") public interface EducationMapper { Education toEntity(CreateEducationRequest request); EducationResponse toResponse(Education entity); void updateEntity(UpdateEducationRequest request, @MappingTarget Education entity); }
