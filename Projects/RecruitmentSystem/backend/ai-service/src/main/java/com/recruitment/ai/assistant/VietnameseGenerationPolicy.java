package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class VietnameseGenerationPolicy {

    public static final String VIETNAMESE_OUTPUT_CONTRACT = """
            YÊU CẦU NGÔN NGỮ BẮT BUỘC:
            - Mọi nội dung diễn giải dành cho người dùng phải viết bằng tiếng Việt tự nhiên, chuyên nghiệp và dễ hiểu.
            - Không trả lời bằng tiếng Anh hoặc ngôn ngữ khác, kể cả khi dữ liệu đầu vào dùng ngôn ngữ khác.
            - Giữ nguyên tên công nghệ, framework, phần mềm, chứng chỉ, công ty, chức danh phổ biến và danh từ riêng khi cần thiết, ví dụ Java, Spring Boot, Docker, AWS, Backend Developer.
            - Không dịch máy móc tên kỹ thuật và không tự thêm dữ kiện.
            - Chỉ trả về một đối tượng JSON đúng schema; giữ nguyên toàn bộ tên trường JSON và các giá trị enum được yêu cầu.
            """;

    public static final String STRONG_RETRY_CONTRACT = """
            LẦN TRẢ LỜI TRƯỚC KHÔNG ĐẠT YÊU CẦU NGÔN NGỮ. Hãy viết lại TOÀN BỘ phần diễn giải bằng tiếng Việt có dấu.
            Tuyệt đối không để câu giải thích tiếng Anh. Chỉ giữ nguyên tên kỹ thuật, framework, phần mềm, công ty,
            chứng chỉ, chức danh phổ biến, tên riêng, tên trường JSON và enum. Trả về đúng một đối tượng JSON theo schema.
            """;

    private static final Set<String> CONTROL_VALUES = Set.of(
            "HIGH", "MEDIUM", "LOW", "EASY", "HARD", "vi", "CANDIDATE", "RECRUITER"
    );

    private final VietnameseResponsePolicy responsePolicy;
    private final ObjectMapper objectMapper;

    public String applyContract(String systemPrompt, String outputSchema) {
        return systemPrompt + "\n\n" + VIETNAMESE_OUTPUT_CONTRACT + "\nRequired JSON Schema: " + outputSchema;
    }

    public StructuredGenerationResult generate(
            StructuredGenerationProvider provider,
            StructuredGenerationRequest request,
            String operation,
            String deterministicFallback
    ) {
        StructuredGenerationResult first = provider.generate(request);
        if (isVietnameseJson(first.structuredOutput())) return first;

        log.warn("AI output language check failed; retrying once operation={} correlationId={}",
                operation, request.correlationId());
        StructuredGenerationRequest retry = new StructuredGenerationRequest(
                request.model(),
                request.systemPrompt() + "\n\n" + STRONG_RETRY_CONTRACT,
                STRONG_RETRY_CONTRACT + "\n<CauTraLoiCanVietLai>\n" + bounded(first.structuredOutput())
                        + "\n</CauTraLoiCanVietLai>",
                request.outputSchema(),
                request.correlationId(),
                request.maxOutputTokens()
        );
        StructuredGenerationResult second = provider.generate(retry);
        if (isVietnameseJson(second.structuredOutput())) return second;

        log.warn("AI output remained outside Vietnamese policy; using deterministic fallback operation={} correlationId={}",
                operation, request.correlationId());
        return new StructuredGenerationResult(
                second.providerName(), second.model(), deterministicFallback,
                first.inputTokens() + second.inputTokens(), first.outputTokens() + second.outputTokens()
        );
    }

    public boolean isVietnameseJson(String output) {
        try {
            JsonNode root = objectMapper.readTree(output);
            if (root == null || !root.isContainerNode()) return false;
            List<String> text = new ArrayList<>();
            collectNaturalLanguage(root, text);
            return !text.isEmpty() && responsePolicy.isVietnameseNaturalLanguage(String.join(" ", text));
        } catch (Exception exception) {
            return false;
        }
    }

    private void collectNaturalLanguage(JsonNode node, List<String> target) {
        if (node.isTextual()) {
            String value = node.asText().trim();
            if (!value.isBlank() && !CONTROL_VALUES.contains(value)) target.add(value);
            return;
        }
        if (node.isContainerNode()) node.elements().forEachRemaining(child -> collectNaturalLanguage(child, target));
    }

    private String bounded(String value) {
        if (value == null) return "";
        return value.substring(0, Math.min(value.length(), 8000));
    }
}
