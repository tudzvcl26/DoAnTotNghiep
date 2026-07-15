package com.recruitment.user.dto.response;
import lombok.Builder; import lombok.Getter; import java.util.List; import java.util.UUID;
@Getter @Builder public class ProfileSummaryResponse { private UUID userId; private String displayName; private String headline; private String cityName; private String countryCode; private List<String> skills; private Integer completionScore; }
