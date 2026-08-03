package com.recruitment.application.mapper;

import com.recruitment.application.dto.response.ApplicationResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.entity.Application;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationResponse toResponse(Application application);

    @BeanMapping(ignoreUnmappedSourceProperties = "coverLetter")
    ApplicationSummaryResponse toSummaryResponse(Application application);

}
