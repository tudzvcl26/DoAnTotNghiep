package com.recruitment.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.recruitment.ai.exception.BusinessException;
import com.recruitment.ai.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Task-specific prose composed only from persisted CV/JD/match evidence. */
@Component
@RequiredArgsConstructor
public class GroundedAssistantComposer {
    private final ObjectMapper mapper;

    public String candidate(String taskName, String contextJson) {
        try {
            JsonNode context = mapper.readTree(contextJson);
            CandidateAssistantTask task = CandidateAssistantTask.valueOf(taskName);
            List<String> skills = values(context.path("resumeFacts").path("technicalSkills"));
            List<String> gaps = values(context.path("deterministicMatchContext")
                    .path("authoritativeMatch").path("missingSkills"));
            String jobTitle = context.path("deterministicMatchContext").path("publishedJob")
                    .path("title").asText("").strip();
            String skill = first(skills, "kỹ năng đã có trong CV");
            String gap = first(gaps, "kỹ năng còn thiếu minh chứng");
            ObjectNode out = base();
            switch (task) {
                case CAREER_ROADMAP -> {
                    out.put("summary", "Lộ trình dựa trên khoảng trống đã xác định giữa CV và công việc đã chọn.");
                    add(out, "recommendations",
                            "0–3 tháng — Mục tiêu: củng cố " + gap + "; Hành động: học có hướng dẫn và làm bài thực hành nhỏ; Đầu ra: mã nguồn, hướng dẫn chạy và bài tự kiểm tra.",
                            "3–6 tháng — Mục tiêu: áp dụng " + gap + " cùng " + skill + "; Hành động: hoàn thiện một sản phẩm minh họa; Đầu ra: demo chạy được, README và kiểm thử.");
                    add(out, "risks", "Chỉ bổ sung kỹ năng hoặc sản phẩm vào CV sau khi đã thực hiện và có minh chứng thật.");
                    add(out, "nextSteps", "Chọn một đầu ra cho giai đoạn 0–3 tháng và ghi lịch kiểm tra hàng tuần.");
                }
                case LEARNING_ROADMAP -> {
                    out.put("summary", "Kế hoạch học tập ưu tiên " + gap + " theo khoảng trống đã xác định.");
                    add(out, "recommendations",
                            "Tuần 1–2: học kiến thức nền của " + gap + " và ghi lại ví dụ tối thiểu.",
                            "Tuần 3–4: thực hành tích hợp " + gap + " với " + skill + "; bài tập là một chức năng có thể chạy độc lập.",
                            "Tuần 5–6: hoàn thiện kiểm thử, README và tự kiểm tra theo tiêu chí chạy đúng, lỗi được xử lý và cách làm được giải thích rõ.");
                    add(out, "risks", "Không ghi nhận đã thành thạo nếu chưa hoàn tất bài thực hành và tiêu chí tự kiểm tra.");
                    add(out, "nextSteps", "Tạo lịch học theo tuần và lưu minh chứng sau mỗi bài tập.");
                }
                case SKILL_ROADMAP -> {
                    out.put("summary", gaps.isEmpty() ? "Chưa có khoảng trống kỹ năng được xác định từ công việc đã chọn."
                            : "Khoảng trống ưu tiên được lấy trực tiếp từ kết quả đối chiếu CV và JD.");
                    ArrayNode recommendations = out.putArray("recommendations");
                    (gaps.isEmpty() ? List.of("kỹ năng chưa có minh chứng") : gaps.stream().limit(2).toList())
                            .forEach(value -> recommendations.add("Khoảng trống: " + value + "; Hành động: làm bài thực hành có phạm vi rõ; Đầu ra: mã nguồn, README và kiểm thử chạy được."));
                    add(out, "risks", "Thiếu minh chứng trong CV không khẳng định bạn chưa có năng lực này.");
                    add(out, "nextSteps", "Ưu tiên tối đa hai khoảng trống và chỉ cập nhật CV sau khi có đầu ra thật.");
                }
                case CERTIFICATE_RECOMMENDATION -> {
                    out.put("summary", "Chứng chỉ chỉ là gợi ý có điều kiện dựa trên công nghệ đã xuất hiện trong CV.");
                    add(out, "recommendations", "Nếu bạn cần xác nhận kiến thức " + skill + ", hãy so sánh một chứng chỉ liên quan về đề cương, hình thức thi và mức phù hợp trước khi đăng ký.");
                    add(out, "risks", "Không ghi chứng chỉ vào CV trước khi đã hoàn thành và có thông tin xác minh.");
                    add(out, "nextSteps", "Đọc đề cương chính thức và làm bài đánh giá thử trước khi quyết định.");
                }
                case PORTFOLIO_RECOMMENDATION -> {
                    out.put("summary", "Đây là dự án portfolio đề xuất để tạo minh chứng mới, chưa phải thành tích hiện có.");
                    add(out, "recommendations", "Đề xuất dự án dùng " + skill + (gaps.isEmpty() ? "" : " và luyện thêm " + gap) + ": xác định ba chức năng nhỏ, tạo demo, viết README về cách chạy và thêm tiêu chí kiểm thử.");
                    add(out, "risks", "Không mô tả dự án đề xuất như dự án đã hoàn thành.");
                    add(out, "nextSteps", "Chốt phạm vi tối thiểu và chỉ công bố khi demo cùng README hoạt động.");
                }
                case JOB_SEARCH_ADVICE -> {
                    String role = jobTitle.isBlank() ? "vai trò phù hợp với kỹ năng trong CV" : jobTitle;
                    out.put("summary", "Chiến lược tìm việc dựa trên vai trò và bằng chứng hiện có trong CV.");
                    add(out, "recommendations", "Tìm vị trí hoặc chức danh gần với " + role + "; dùng từ khóa " + skill + " và đọc yêu cầu trước khi ứng tuyển.");
                    add(out, "risks", "Không suy đoán công ty đang tuyển hoặc ứng tuyển khi yêu cầu cốt lõi chưa phù hợp.");
                    add(out, "nextSteps", "Lập lịch theo dõi hai lần mỗi tuần và ghi trạng thái từng tin tuyển dụng.");
                }
                case RESUME_IMPROVEMENT -> {
                    out.put("summary", "Các chỉnh sửa CV dưới đây chỉ yêu cầu làm rõ dữ liệu và minh chứng đang có.");
                    add(out, "recommendations",
                            "CV: nhóm các kỹ năng đã có như " + skill + " theo loại và mức độ có thể chứng minh.",
                            "CV: với mỗi kinh nghiệm hoặc dự án có thật, mô tả vai trò, việc đã làm và đầu ra có thể kiểm tra.",
                            "CV: bổ sung liên kết hoặc số liệu chỉ khi chúng tồn tại và có thể xác minh.");
                    add(out, "risks", "Không tự tạo thành tích, số liệu, dự án hoặc kinh nghiệm để làm CV đầy hơn.");
                    add(out, "nextSteps", "Đối chiếu từng câu sửa với CV gốc trước khi lưu.");
                }
            }
            return mapper.writeValueAsString(out);
        } catch (BusinessException error) { throw error; }
        catch (Exception error) { throw new BusinessException(ErrorCode.ASSISTANT_RESPONSE_INVALID); }
    }

    public String recruiterJobSummary(String contextJson) {
        try {
            JsonNode job = mapper.readTree(contextJson).path("publishedJob");
            String title = job.path("title").asText("").strip();
            String responsibilities = job.path("responsibilities").asText("").strip();
            String requirements = job.path("requirements").asText("").strip();
            String level = job.path("experienceLevel").asText("").strip();
            if (title.isBlank()) throw new BusinessException(ErrorCode.ASSISTANT_CONTEXT_INVALID);
            ObjectNode out = base();
            out.put("summary", "Tóm tắt công việc " + title + " cho nhà tuyển dụng.");
            add(out, "recommendations", "Vai trò: " + title + ".",
                    "Trách nhiệm/Nhiệm vụ: " + present(responsibilities) + ".",
                    "Yêu cầu: " + present(requirements) + (level.isBlank() ? "." : "; mức kinh nghiệm: " + level + "."));
            add(out, "risks", "Kiểm tra lại nội dung công việc trước khi sử dụng bản tóm tắt.");
            add(out, "nextSteps", "Đối chiếu bản tóm tắt với tin tuyển dụng đã xuất bản.");
            return mapper.writeValueAsString(out);
        } catch (BusinessException error) { throw error; }
        catch (Exception error) { throw new BusinessException(ErrorCode.ASSISTANT_RESPONSE_INVALID); }
    }

    private ObjectNode base() { return mapper.createObjectNode(); }
    private void add(ObjectNode node, String field, String... values) {
        ArrayNode array = node.putArray(field);
        for (String value : values) array.add(value);
    }
    private List<String> values(JsonNode node) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (node.isArray()) node.forEach(item -> {
            String value = item.isTextual() ? item.asText("") : item.path("name").asText("");
            if (!value.isBlank()) result.add(value.strip());
        });
        return new ArrayList<>(result);
    }
    private String first(List<String> values, String fallback) { return values.isEmpty() ? fallback : values.get(0); }
    private String present(String value) { return value.isBlank() ? "chưa có nội dung trong tin đã xuất bản" : value; }
}
