package com.recruitment.ai.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_COMMON_500", "Hệ thống AI đang gặp sự cố. Vui lòng thử lại sau.", true),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "AI_COMMON_400", "Yêu cầu chưa hợp lệ.", false),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "AI_COMMON_001", "Dữ liệu gửi lên chưa hợp lệ.", false),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AI_AUTH_401", "Bạn cần đăng nhập để sử dụng chức năng này.", false),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AI_AUTH_403", "Bạn không có quyền sử dụng chức năng này.", false),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "AI_COMMON_003", "Yêu cầu xung đột với dữ liệu hiện có.", false),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_TASK_001", "Không tìm thấy tác vụ AI.", false),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_RESUME_001", "Không tìm thấy CV.", false),
    RESUME_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "AI_RESUME_002", "Vui lòng chọn tệp CV.", false),
    RESUME_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "AI_RESUME_003", "Tệp CV vượt quá giới hạn 10 MB.", false),
    RESUME_FILE_TYPE_UNSUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "AI_RESUME_004", "Chỉ hỗ trợ tệp CV PDF, DOCX hoặc TXT.", false),
    RESUME_EXTRACTION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "AI_RESUME_005", "Không thể trích xuất nội dung CV.", false),
    RESUME_TEXT_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "AI_RESUME_006", "CV không có nội dung văn bản có thể đọc được.", false),
    RESUME_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_RESUME_007", "Không tìm thấy kết quả phân tích CV.", false),
    RESUME_ANALYSIS_INVALID(HttpStatus.BAD_GATEWAY, "AI_RESUME_008", "Mô hình AI trả về kết quả phân tích CV không hợp lệ.", true),
    RESUME_FILE_READ_FAILED(HttpStatus.BAD_REQUEST, "AI_RESUME_009", "Không thể đọc tệp CV.", false),
    RESUME_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_RESUME_010", "Chỉ dẫn phân tích CV chưa được cấu hình.", true),
    RESUME_MODEL_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_RESUME_011", "Mô hình phân tích CV chưa được cấu hình.", true),
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_MATCH_001", "Không tìm thấy kết quả đánh giá độ phù hợp.", false),
    MATCH_RESUME_NOT_ANALYZED(HttpStatus.CONFLICT, "AI_MATCH_002", "CV cần được phân tích trước khi đánh giá độ phù hợp.", false),
    MATCH_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_MATCH_003", "Không tìm thấy việc làm.", false),
    MATCH_JOB_NOT_PUBLISHED(HttpStatus.CONFLICT, "AI_MATCH_004", "Chỉ có thể đánh giá việc làm đang được đăng tuyển.", false),
    MATCH_UPSTREAM_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_MATCH_005", "Dịch vụ dữ liệu cần thiết đang tạm thời không khả dụng.", true),
    EXPLANATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_EXPLANATION_001", "Không tìm thấy phần giải thích độ phù hợp.", false),
    EXPLANATION_INVALID(HttpStatus.BAD_GATEWAY, "AI_EXPLANATION_002", "Mô hình AI trả về phần giải thích không hợp lệ.", true),
    EXPLANATION_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_EXPLANATION_003", "Chỉ dẫn giải thích độ phù hợp chưa được cấu hình.", true),
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_INTERVIEW_001", "Không tìm thấy nội dung chuẩn bị phỏng vấn.", false),
    INTERVIEW_INVALID(HttpStatus.BAD_GATEWAY, "AI_INTERVIEW_002", "Mô hình AI trả về nội dung chuẩn bị phỏng vấn không hợp lệ.", true),
    INTERVIEW_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_INTERVIEW_003", "Chỉ dẫn chuẩn bị phỏng vấn chưa được cấu hình.", true),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_RECOMMENDATION_001", "Không tìm thấy gợi ý.", false),
    RECOMMENDATION_INVALID(HttpStatus.BAD_GATEWAY, "AI_RECOMMENDATION_002", "Mô hình AI trả về gợi ý không hợp lệ.", true),
    RECOMMENDATION_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_RECOMMENDATION_003", "Chỉ dẫn tạo gợi ý chưa được cấu hình.", true),
    RECOMMENDATION_RESUME_REQUIRED(HttpStatus.BAD_REQUEST, "AI_RECOMMENDATION_004", "Cần có CV đã phân tích để tạo gợi ý.", false),
    RECOMMENDATION_CONSENT_REQUIRED(HttpStatus.CONFLICT, "AI_RECOMMENDATION_005", "Bạn cần đồng ý sử dụng dữ liệu để nhận gợi ý việc làm.", false),
    ASSISTANT_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "AI_ASSISTANT_001", "Mô hình AI trả về nội dung trợ lý không hợp lệ.", true),
    ASSISTANT_PROMPT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_ASSISTANT_002", "Chỉ dẫn trợ lý chưa được cấu hình.", true),
    ASSISTANT_CONTEXT_INVALID(HttpStatus.BAD_REQUEST, "AI_ASSISTANT_003", "Tác vụ đã chọn cần thêm dữ liệu ngữ cảnh.", false),
    CAREER_MESSAGE_INVALID(HttpStatus.BAD_REQUEST, "AI_CAREER_001", "Vui lòng nhập câu hỏi cụ thể hơn để mình có thể hỗ trợ.", false),
    CAREER_CONTEXT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_CAREER_002", "Hiện tại chưa thể tải dữ liệu nghề nghiệp của bạn. Vui lòng thử lại sau.", true),
    CAREER_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "AI_CAREER_003", "Mô hình AI trả về nội dung không hợp lệ.", true),
    CAREER_MODEL_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "AI_CAREER_004", "Mô hình trợ lý nghề nghiệp chưa được cấu hình.", true),
    PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_001", "Hiện tại trợ lý AI đang tạm thời không khả dụng. Vui lòng thử lại sau.", true),
    PROVIDER_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI_PROVIDER_002", "Yêu cầu AI mất quá nhiều thời gian xử lý. Vui lòng thử lại.", true),
    PROVIDER_MODEL_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_PROVIDER_003", "Mô hình AI được cấu hình hiện không khả dụng. Vui lòng thử lại sau.", true),
    PROVIDER_EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_004", "Mô hình AI không trả về nội dung hợp lệ.", true),
    STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_STORAGE_001", "Kho lưu trữ AI đang tạm thời không khả dụng.", true);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final boolean retryable;

}
