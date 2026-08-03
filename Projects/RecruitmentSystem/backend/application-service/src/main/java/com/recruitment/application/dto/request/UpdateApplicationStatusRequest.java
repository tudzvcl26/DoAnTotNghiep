package com.recruitment.application.dto.request;

import com.recruitment.application.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateApplicationStatusRequest {

    @NotNull
    private ApplicationStatus status;

    @Size(max = 50)
    private String reasonCode;

    @Size(max = 1000)
    private String reasonDetail;

}
