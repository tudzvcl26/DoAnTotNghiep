package com.recruitment.user.dto.request;

import com.recruitment.user.entity.ProfileVisibility;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    @NotBlank @Size(max = 150) private String displayName;
    @Size(max = 255) private String headline;
    @Size(max = 5000) private String summary;
    @Pattern(regexp = "^[A-Z]{2}$") private String countryCode;
    @Size(max = 50) private String provinceCode;
    @Size(max = 120) private String cityName;
    @Size(max = 120) private String districtName;
    @Email @Size(max = 255) private String contactEmail;
    @Size(max = 30) private String contactPhone;
    private ProfileVisibility profileVisibility;
    @Max(Long.MAX_VALUE) private Long version;
}
