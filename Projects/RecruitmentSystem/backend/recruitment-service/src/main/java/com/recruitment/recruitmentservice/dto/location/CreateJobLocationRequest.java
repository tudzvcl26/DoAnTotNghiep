package com.recruitment.recruitmentservice.dto.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateJobLocationRequest {

    @NotNull
    private UUID jobId;

    @NotBlank
    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String district;

    @Size(max = 255)
    private String address;

    private Boolean primaryLocation = false;

}