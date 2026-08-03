package com.recruitment.ai.service.document;

import com.recruitment.ai.entity.enums.ResumeFileType;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

@Component
public class TxtDocumentExtractor implements DocumentExtractor {

    @Override
    public ResumeFileType supportedType() {
        return ResumeFileType.TXT;
    }

    @Override
    public String extract(byte[] documentBytes) throws Exception {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(documentBytes))
                .toString();
    }
}
