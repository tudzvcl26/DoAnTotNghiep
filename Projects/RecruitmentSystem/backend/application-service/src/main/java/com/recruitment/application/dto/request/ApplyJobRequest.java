package com.recruitment.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ApplyJobRequest {

    @NotNull
    private UUID jobId;

    @Size(max = 5000)
    private String coverLetter;

}
