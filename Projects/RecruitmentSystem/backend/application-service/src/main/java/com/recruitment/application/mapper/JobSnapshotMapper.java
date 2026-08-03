package com.recruitment.application.mapper;

import com.recruitment.application.dto.response.JobSnapshotResponse;
import com.recruitment.application.entity.JobSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobSnapshotMapper {

    JobSnapshotResponse toResponse(JobSnapshot jobSnapshot);

}
