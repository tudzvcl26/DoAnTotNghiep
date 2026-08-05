package com.recruitment.ai.provider;

import com.recruitment.ai.config.AiProviderProperties;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultModelRouterTest {

    @Test
    void selectsOllamaOnlyFromConfiguration() {
        AiProviderProperties properties = new AiProviderProperties();
        properties.setType(ProviderType.OLLAMA);
        OpenAiStructuredGenerationProvider openAi = mock(OpenAiStructuredGenerationProvider.class);
        OllamaStructuredGenerationProvider ollama = mock(OllamaStructuredGenerationProvider.class);
        when(openAi.descriptor()).thenReturn(new ProviderDescriptor("openai", "OPENAI", true));
        when(ollama.descriptor()).thenReturn(new ProviderDescriptor("ollama", "OLLAMA", true));

        StructuredGenerationProvider selected = new DefaultModelRouter(
                new NoOpAiProvider(), properties, openAi, ollama
        ).structuredGenerationProvider();

        assertThat(selected).isSameAs(ollama);
    }

    @Test
    void selectsOpenAiOnlyFromConfiguration() {
        AiProviderProperties properties = new AiProviderProperties();
        properties.setType(ProviderType.OPENAI);
        OpenAiStructuredGenerationProvider openAi = mock(OpenAiStructuredGenerationProvider.class);
        OllamaStructuredGenerationProvider ollama = mock(OllamaStructuredGenerationProvider.class);
        when(openAi.descriptor()).thenReturn(new ProviderDescriptor("openai", "OPENAI", true));
        when(ollama.descriptor()).thenReturn(new ProviderDescriptor("ollama", "OLLAMA", true));

        StructuredGenerationProvider selected = new DefaultModelRouter(
                new NoOpAiProvider(), properties, openAi, ollama
        ).structuredGenerationProvider();

        assertThat(selected).isSameAs(openAi);
    }
}
