package com.recruitment.ai.matching.client;

import com.recruitment.ai.matching.model.JobSnapshot;

import java.util.UUID;
import java.util.List;

public interface JobGateway {
    JobSnapshot getJob(UUID jobId, String accessToken);

    List<JobSnapshot> getPublishedJobs(String accessToken);
}
