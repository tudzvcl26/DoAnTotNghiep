package com.recruitment.company.validation;

public final class CompanyValidationMessages {

    private CompanyValidationMessages() {
    }

    public static final String COMPANY_NAME_REQUIRED =
            "Company name is required.";

    public static final String COMPANY_NAME_MAX =
            "Company name must not exceed 255 characters.";

    public static final String DESCRIPTION_MAX =
            "Description must not exceed 5000 characters.";

    public static final String WEBSITE_MAX =
            "Website must not exceed 255 characters.";

    public static final String EMAIL_INVALID =
            "Invalid email address.";

    public static final String EMAIL_MAX =
            "Email must not exceed 255 characters.";

    public static final String PHONE_MAX =
            "Phone must not exceed 50 characters.";

    public static final String TAX_CODE_MAX =
            "Tax code must not exceed 100 characters.";

    public static final String LOGO_URL_MAX =
            "Logo URL must not exceed 500 characters.";

    public static final String BANNER_URL_MAX =
            "Banner URL must not exceed 500 characters.";

}