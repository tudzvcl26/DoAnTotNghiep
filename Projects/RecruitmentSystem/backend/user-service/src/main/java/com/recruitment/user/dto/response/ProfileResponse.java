package com.recruitment.user.dto.response;
import com.recruitment.user.entity.ProfileStatus;
import com.recruitment.user.entity.ProfileVisibility;
import lombok.Builder; import lombok.Getter;
import java.time.LocalDateTime; import java.util.UUID;
@Getter @Builder public class ProfileResponse {
    private UUID id; private UUID userId; private String displayName; private String headline; private String summary;
    private String countryCode; private String provinceCode; private String cityName; private String districtName;
    private String contactEmail; private String contactPhone; private ProfileVisibility profileVisibility;
    private ProfileStatus profileStatus; private Integer completionScore; private LocalDateTime completionCalculatedAt;
    private Long version; private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
