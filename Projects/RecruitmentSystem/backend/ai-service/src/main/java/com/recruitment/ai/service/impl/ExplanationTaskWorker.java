package com.recruitment.ai.service.impl;

import com.recruitment.ai.entity.enums.AiTaskStatus;
import com.recruitment.ai.repository.AiTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/** One bounded inference at a time per instance; DB claims coordinate instances. */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.generation.worker-enabled", havingValue = "true", matchIfMissing = true)
public class ExplanationTaskWorker {
    private final AiTaskRepository tasks;
    private final ExplanationInterviewServiceImpl service;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverInterruptedTasks() {
        service.recoverInterruptedTasksAfterRestart();
    }

    @Scheduled(fixedDelayString = "${ai.generation.poll-delay-ms:1000}")
    public void poll() {
        service.expireAbandonedTasks();
        for (var task : tasks.findTop20ByTaskTypeInAndStatusOrderByCreatedAtAsc(
                java.util.List.of("MATCH_EXPLANATION", "INTERVIEW_PREPARATION"), AiTaskStatus.PENDING)) {
            if (service.processQueuedExplanation(task.getId())) break;
        }
    }
}
