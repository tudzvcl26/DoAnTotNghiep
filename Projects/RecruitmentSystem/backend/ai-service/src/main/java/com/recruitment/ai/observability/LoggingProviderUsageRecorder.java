package com.recruitment.ai.observability;

import com.recruitment.ai.provider.ProviderUsage;
import com.recruitment.ai.provider.ProviderUsageRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingProviderUsageRecorder implements ProviderUsageRecorder {

    @Override
    public void record(ProviderUsage usage) {
        log.info(
                "AI provider usage provider={} model={} operation={} inputTokens={} outputTokens={} durationMs={} success={} correlationId={}",
                usage.providerName(),
                usage.model(),
                usage.operation(),
                usage.inputTokens(),
                usage.outputTokens(),
                usage.durationMillis(),
                usage.successful(),
                usage.correlationId()
        );
    }

}
