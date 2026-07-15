package com.recruitment.user.dto.request;

import com.recruitment.user.entity.SkillLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Getter @Setter
public class CreateSkillRequest {
    @NotBlank @Size(max = 150) private String skillName;
    private SkillLevel skillLevel;
    @DecimalMin("0.0") @DecimalMax("99.9") private BigDecimal yearsExperience;
}
