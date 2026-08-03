package com.recruitment.application.client;

import java.util.Optional;
import java.util.UUID;

public interface JobClient {

    Optional<JobClientDto> getJobById(UUID jobId);

}
