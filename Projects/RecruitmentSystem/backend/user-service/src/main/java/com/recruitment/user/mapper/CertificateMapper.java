package com.recruitment.user.mapper;

import com.recruitment.user.dto.request.CreateCertificateRequest;
import com.recruitment.user.dto.request.UpdateCertificateRequest;
import com.recruitment.user.dto.response.CertificateResponse;
import com.recruitment.user.entity.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    Certificate toEntity(CreateCertificateRequest request);

    CertificateResponse toResponse(Certificate entity);

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
            UpdateCertificateRequest request,
            @MappingTarget Certificate entity
    );

}