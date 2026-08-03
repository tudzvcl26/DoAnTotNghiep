package com.recruitment.application.mapper;

import com.recruitment.application.dto.response.ResumeSnapshotResponse;
import com.recruitment.application.entity.ResumeSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResumeSnapshotMapper {

    ResumeSnapshotResponse toResponse(ResumeSnapshot resumeSnapshot);

}
