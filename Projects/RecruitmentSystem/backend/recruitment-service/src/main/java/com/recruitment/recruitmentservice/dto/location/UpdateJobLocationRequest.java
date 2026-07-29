package com.recruitment.recruitmentservice.dto.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateJobLocationRequest {

    @NotBlank
    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String district;

    @Size(max = 255)
    private String address;

    private Boolean primaryLocation;

}