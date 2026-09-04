package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.provider.llm.StructuredGenerationProvider;
import com.recruitment.ai.provider.llm.StructuredGenerationRequest;
import com.recruitment.ai.provider.llm.StructuredGenerationResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VietnameseGenerationPolicyTest {

    @Test void semanticFailureSharesTheSingleRetryBudgetAndNeverPersistsTheHiringClaim() {
        StructuredGenerationProvider provider = mock(StructuredGenerationProvider.class);
        when(provider.generate(any())).thenReturn(result("{\"summary\":\"Bạn đã được tuyển dụng cho vị trí Java.\"}"));
        var acceptance = new CandidateAnswerPolicy(new ObjectMapper());
        String fallback = "{\"summary\":\"Chưa có tư vấn đã được xác minh.\"}";
        var response = policy.generateValidated(provider, request(), "CANDIDATE_ASSISTANT", fallback,
                output -> acceptance.accepts(output, "SKILL_ROADMAP"), CandidateAnswerPolicy.CONTRACT);
        assertThat(response.structuredOutput()).isEqualTo(fallback);
        assertThat(response.inputTokens()).isEqualTo(20);
        verify(provider, times(2)).generate(any());
    }

    private final VietnameseGenerationPolicy policy = new VietnameseGenerationPolicy(
            new VietnameseResponsePolicy(), new ObjectMapper());

    @Test
    void rejectsEnglishProseFieldDespiteVietnameseOtherFieldAndAllowsTechnicalNames() {
        assertThat(policy.isVietnameseJson("{\"summary\":\"Bạn nên cải thiện kỹ năng của mình.\","
                + "\"advice\":\"You should improve your skills and focus on career experience.\"}")).isFalse();
        assertThat(policy.isVietnameseJson("{\"summary\":\"Bạn nên bổ sung dự án Java.\",\"technology\":\"Spring Boot\",\"priority\":\"HIGH\"}")).isTrue();
    }

    @Test
    void addsMandatoryVietnameseJsonContractToPrompt() {
        String prompt = policy.applyContract("Không thay đổi điểm.", "{\"type\":\"object\"}");

        assertThat(prompt).contains("Mọi nội dung diễn giải dành cho người dùng phải viết bằng tiếng Việt")
                .contains("Giữ nguyên tên công nghệ")
                .contains("Chỉ trả về một đối tượng JSON đúng schema");
    }

    @Test void resumeFactsAllowProperNamesButNeverPersistEnglishProseAfterFailedRewrite() {
        assertThat(policy.isVietnameseResumeJson("{\"fullName\":\"Morgan QA\",\"summary\":null,\"education\":[\"Bachelor of Computer Science at Example University\"]}")).isTrue();
        assertThat(policy.isVietnameseResumeJson("{\"fullName\":\"Morgan QA\",\"summary\":\"Tóm tắt hồ sơ dựa trên dữ liệu đã cung cấp.\",\"experience\":[\"2 years at Sample Studio\"],\"projects\":[\"Accessible booking interface\"]}")).isTrue();
        assertThat(policy.isVietnameseResumeJson("{\"fullName\":\"Morgan QA\",\"summary\":\"Backend software engineer with five years of professional experience.\",\"experience\":[\"2 years at Sample Studio\"]}")).isFalse();
        StructuredGenerationProvider provider = mock(StructuredGenerationProvider.class);
        when(provider.generate(any())).thenReturn(result("{\"summary\":\"Backend software engineer with 5 years of experience building Java services.\"}"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> policy.generateResume(provider, request()))
                .isInstanceOf(com.recruitment.ai.exception.BusinessException.class);
        verify(provider, times(2)).generate(any());
    }

    @Test
    void englishOutputTriggersExactlyOneStrongerRetry() {
        StructuredGenerationProvider provider = mock(StructuredGenerationProvider.class);
        when(provider.generate(any()))
                .thenReturn(result("{\"summary\":\"You should improve your technical skills and experience.\"}"))
                .thenReturn(result("{\"summary\":\"Bạn nên cải thiện kỹ năng Java và kinh nghiệm dự án.\"}"));
        StructuredGenerationRequest request = request();

        StructuredGenerationResult generated = policy.generate(
                provider, request, "CANDIDATE_ASSISTANT", "{\"summary\":\"Nội dung dự phòng bằng tiếng Việt.\"}");

        assertThat(generated.structuredOutput()).contains("Bạn nên cải thiện", "Java");
        ArgumentCaptor<StructuredGenerationRequest> requests = ArgumentCaptor.forClass(StructuredGenerationRequest.class);
        verify(provider, times(2)).generate(requests.capture());
        assertThat(requests.getAllValues().get(1).systemPrompt()).contains("LẦN TRẢ LỜI TRƯỚC KHÔNG ĐẠT")
                .contains("Tuyệt đối không để câu giải thích tiếng Anh");
        assertThat(requests.getAllValues().get(1).userPrompt()).contains(request.userPrompt());
    }

    @Test
    void usesDeterministicVietnameseFallbackWhenSingleRetryStillFails() {
        StructuredGenerationProvider provider = mock(StructuredGenerationProvider.class);
        when(provider.generate(any())).thenReturn(result(
                "{\"summary\":\"The candidate should improve skills and experience for this role.\"}"));
        String fallback = "{\"summary\":\"Vui lòng thử lại để nhận tư vấn nghề nghiệp bằng tiếng Việt.\"}";

        StructuredGenerationResult generated = policy.generate(provider, request(), "CANDIDATE_ASSISTANT", fallback);

        assertThat(generated.structuredOutput()).isEqualTo(fallback);
        verify(provider, times(2)).generate(any());
    }

    private StructuredGenerationRequest request() {
        return new StructuredGenerationRequest("qwen", "system", "context", "{}", "correlation");
    }

    private StructuredGenerationResult result(String output) {
        return new StructuredGenerationResult("ollama", "qwen", output, 10, 10);
    }
}
