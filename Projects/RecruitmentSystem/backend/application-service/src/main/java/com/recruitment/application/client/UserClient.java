package com.recruitment.application.client;

import java.util.Optional;
import java.util.UUID;

public interface UserClient {

    Optional<UserClientDto> getCandidateProfile(UUID candidateId, String bearerToken);

}
