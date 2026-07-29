package com.recruitment.recruitmentservice.dto.location;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class JobLocationResponse {

    private UUID id;

    private UUID jobId;

    private String province;

    private String district;

    private String address;

    private Boolean primaryLocation;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}