package com.recruitment.user.dto.response;
import com.recruitment.user.entity.LanguageLevel; import lombok.Builder; import lombok.Getter; import java.util.UUID;
@Getter @Builder public class LanguageResponse { private UUID id; private UUID languageId; private String languageCode; private String displayName; private LanguageLevel languageLevel; private Long version; }
