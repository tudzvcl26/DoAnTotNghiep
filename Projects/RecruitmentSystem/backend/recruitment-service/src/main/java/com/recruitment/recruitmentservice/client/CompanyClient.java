package com.recruitment.recruitmentservice.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyClient {

    Optional<CompanyClientDto> getCompanyById(UUID companyId);

    List<CompanyClientDto> getCompaniesByOwner(UUID ownerId, String accessToken);

}
