package com.recruitment.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class OllamaConfiguration {

    @Bean("ollamaHttpClient")
    HttpClient ollamaHttpClient(OllamaProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

}
