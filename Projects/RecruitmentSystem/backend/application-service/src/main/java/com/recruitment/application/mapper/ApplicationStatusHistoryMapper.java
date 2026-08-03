package com.recruitment.application.mapper;

import com.recruitment.application.dto.response.ApplicationStatusHistoryResponse;
import com.recruitment.application.entity.ApplicationStatusHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationStatusHistoryMapper {

    ApplicationStatusHistoryResponse toResponse(ApplicationStatusHistory history);

    List<ApplicationStatusHistoryResponse> toResponseList(List<ApplicationStatusHistory> histories);

}
