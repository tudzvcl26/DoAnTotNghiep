package com.recruitment.application.mapper;

import com.recruitment.application.dto.response.ApplicationResponse;
import com.recruitment.application.dto.response.ApplicationSummaryResponse;
import com.recruitment.application.entity.Application;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "resumeSnapshot", ignore = true)
    @Mapping(target = "jobSnapshot", ignore = true)
    @Mapping(target = "candidateProfileSnapshot", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    ApplicationResponse toResponse(Application application);

    @BeanMapping(ignoreUnmappedSourceProperties = "coverLetter")
    @Mapping(target = "candidateProfileSnapshot", ignore = true)
    @Mapping(target = "jobSnapshot", ignore = true)
    ApplicationSummaryResponse toSummaryResponse(Application application);

}
