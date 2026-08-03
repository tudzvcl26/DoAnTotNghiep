package com.recruitment.application.client;

import java.util.Optional;
import java.util.UUID;

public interface CompanyClient {

    Optional<CompanyClientDto> getCompanyById(UUID companyId);

}
