package com.recruitment.notification.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTemplateActiveRequest {

    @NotNull(message = "Active value is required.")
    private Boolean active;

}
