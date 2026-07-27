package com.recruitment.company.dto.request;

import com.recruitment.company.enums.CompanySize;
import com.recruitment.company.enums.CompanyType;
import com.recruitment.company.validation.CompanyValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequest {

    @Size(
            max = 255,
            message = CompanyValidationMessages.COMPANY_NAME_MAX
    )
    private String name;

    @Size(
            max = 5000,
            message = CompanyValidationMessages.DESCRIPTION_MAX
    )
    private String description;

    @Size(
            max = 255,
            message = CompanyValidationMessages.WEBSITE_MAX
    )
    private String website;

    @Email(message = CompanyValidationMessages.EMAIL_INVALID)
    @Size(
            max = 255,
            message = CompanyValidationMessages.EMAIL_MAX
    )
    private String email;

    @Size(
            max = 50,
            message = CompanyValidationMessages.PHONE_MAX
    )
    private String phone;

    private CompanyType companyType;

    private CompanySize companySize;

    @Size(
            max = 500,
            message = CompanyValidationMessages.LOGO_URL_MAX
    )
    private String logoUrl;

    @Size(
            max = 500,
            message = CompanyValidationMessages.BANNER_URL_MAX
    )
    private String bannerUrl;

}