package com.recruitment.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.resume")
public class ResumePipelineProperties {

    private DataSize maxFileSize = DataSize.ofMegabytes(10);
    private int maxExtractedCharacters = 200_000;
}
