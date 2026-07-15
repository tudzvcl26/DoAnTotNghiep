package com.recruitment.user.dto.request;
import com.recruitment.user.entity.SocialLinkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter; import lombok.Setter;
@Getter @Setter public class CreateSocialLinkRequest {
    private SocialLinkType linkType;
    @NotBlank @Size(max = 2048) @Pattern(regexp = "https?://.+") private String url;
    @Size(max = 150) private String label;
}
