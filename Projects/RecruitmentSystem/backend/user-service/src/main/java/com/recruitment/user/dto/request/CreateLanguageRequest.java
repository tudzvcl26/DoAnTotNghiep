package com.recruitment.user.dto.request;
import com.recruitment.user.entity.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter; import lombok.Setter;
@Getter @Setter public class CreateLanguageRequest { @NotBlank @Size(max = 20) private String languageCode; private LanguageLevel languageLevel; }
