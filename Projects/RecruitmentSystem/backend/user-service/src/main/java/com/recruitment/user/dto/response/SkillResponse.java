package com.recruitment.user.dto.response;
import com.recruitment.user.entity.SkillLevel; import lombok.Builder; import lombok.Getter; import java.math.BigDecimal; import java.util.UUID;
@Getter @Builder public class SkillResponse { private UUID id; private UUID skillId; private String skillName; private SkillLevel skillLevel; private BigDecimal yearsExperience; private Long version; }
