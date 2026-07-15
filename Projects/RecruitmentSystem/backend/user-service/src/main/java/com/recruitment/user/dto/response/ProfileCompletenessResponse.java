package com.recruitment.user.dto.response;
import lombok.Builder; import lombok.Getter; import java.time.LocalDateTime; import java.util.List;
@Getter @Builder public class ProfileCompletenessResponse { private Integer score; private Boolean completed; private List<String> missingSections; private LocalDateTime calculatedAt; }
