package com.recruitment.ai.mapper;

import com.recruitment.ai.dto.response.AiTaskResponse;
import com.recruitment.ai.entity.AiTask;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AiTaskMapper {

    AiTaskResponse toResponse(AiTask task);

}
