package com.recruitment.application.client;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface CompanyClient {

    Optional<CompanyClientDto> getCompanyById(UUID companyId);

    List<CompanyClientDto> getCompaniesByOwnerId(UUID ownerId, String bearerToken);

}
