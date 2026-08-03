package com.recruitment.ai;

import com.recruitment.ai.provider.ModelRouter;
import com.recruitment.ai.repository.AiTaskRepository;
import com.recruitment.ai.repository.ModelDeploymentRepository;
import com.recruitment.ai.repository.PromptTemplateVersionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AiServiceApplicationTests {

    @Autowired
    private AiTaskRepository aiTaskRepository;

    @Autowired
    private PromptTemplateVersionRepository promptTemplateVersionRepository;

    @Autowired
    private ModelDeploymentRepository modelDeploymentRepository;

    @Autowired
    private ModelRouter modelRouter;

    @Test
    void contextLoadsWithFoundationRepositoriesAndNoOpProviders() {
        assertThat(aiTaskRepository).isNotNull();
        assertThat(promptTemplateVersionRepository).isNotNull();
        assertThat(modelDeploymentRepository).isNotNull();
        assertThat(modelRouter.structuredGenerationProvider().descriptor().available()).isFalse();
        assertThat(modelRouter.embeddingProvider().descriptor().available()).isFalse();
    }

}
