package com.recruitment.ai.assistant;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class CandidateAssistantTaskTest {
    @Test void eachTaskHasDistinctActionableVietnameseInstructions() {
        assertThat(java.util.Arrays.stream(CandidateAssistantTask.values()).map(CandidateAssistantTask::instruction).distinct().count()).isEqualTo(7);
        assertThat(CandidateAssistantTask.CERTIFICATE_RECOMMENDATION.instruction()).contains("chứng chỉ gợi ý", "không phải ứng viên đã sở hữu");
        assertThat(CandidateAssistantTask.PORTFOLIO_RECOMMENDATION.instruction()).contains("README", "chưa phải thành tích");
        assertThat(CandidateAssistantTask.LEARNING_ROADMAP.instruction()).contains("theo tuần", "tự kiểm tra");
    }
}
