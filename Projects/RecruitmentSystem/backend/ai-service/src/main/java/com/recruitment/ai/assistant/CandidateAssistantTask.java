package com.recruitment.ai.assistant;

public enum CandidateAssistantTask {
    CAREER_ROADMAP,
    LEARNING_ROADMAP,
    SKILL_ROADMAP,
    CERTIFICATE_RECOMMENDATION,
    PORTFOLIO_RECOMMENDATION,
    JOB_SEARCH_ADVICE,
    RESUME_IMPROVEMENT;

    public String instruction() {
        return switch (this) {
            case CAREER_ROADMAP -> "Đề xuất vai trò mục tiêu phù hợp nền tảng CV. Bắt buộc có hai giai đoạn '0–3 tháng' và '3–6 tháng'; mỗi giai đoạn ghi rõ Mục tiêu, Hành động và Đầu ra kiểm chứng được. Không đưa quyết định tuyển dụng.";
            case LEARNING_ROADMAP -> "Lập kế hoạch học theo tuần: kiến thức tiên quyết, bài thực hành và tiêu chí tự kiểm tra. Ưu tiên một công nghệ liên quan CV hoặc kỹ năng thiếu đã xác định.";
            case SKILL_ROADMAP -> "Chỉ tập trung vào khoảng trống kỹ năng giữa CV và công việc đã chọn. Chọn tối đa hai khoảng trống có trong deterministicMatchContext.missingSkills; mỗi kỹ năng có lý do, hành động và đầu ra đo được. Nếu không có công việc/match thì nói rõ thiếu dữ liệu, không tự tạo yêu cầu. Không mâu thuẫn JD và không lặp danh sách từ khóa.";
            case CERTIFICATE_RECOMMENDATION -> "Chỉ đề xuất tối đa hai chứng chỉ liên quan công nghệ trong CV, nêu điều kiện và lợi ích. Ghi rõ đây là chứng chỉ gợi ý, không phải ứng viên đã sở hữu; không bịa chi phí hoặc tuyên bố bắt buộc.";
            case PORTFOLIO_RECOMMENDATION -> "Đề xuất một dự án portfolio phù hợp công nghệ đã nêu: chức năng, sản phẩm demo, README và tiêu chí kiểm thử. Nói rõ đó là dự án đề xuất chưa phải thành tích hiện có.";
            case JOB_SEARCH_ADVICE -> "Đề xuất vị trí tìm kiếm, từ khóa chức danh phù hợp, cách chọn tin và lịch theo dõi ứng tuyển. Không bịa công ty đang tuyển hoặc liên kết việc làm.";
            case RESUME_IMPROVEMENT -> "Đưa ba chỉnh sửa CV cụ thể theo dữ liệu: phần cần sửa, cách trình bày và minh chứng cần bổ sung. Không tự tạo thành tích, số liệu hoặc kinh nghiệm.";
        };
    }
}
