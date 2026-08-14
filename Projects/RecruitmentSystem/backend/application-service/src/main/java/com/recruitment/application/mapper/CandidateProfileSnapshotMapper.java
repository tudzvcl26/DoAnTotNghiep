package com.recruitment.application.mapper;

import com.recruitment.application.dto.response.CandidateProfileSnapshotResponse;
import com.recruitment.application.entity.CandidateProfileSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CandidateProfileSnapshotMapper {

    CandidateProfileSnapshotResponse toResponse(CandidateProfileSnapshot snapshot);
}
