package com.recruitment.ai.repository;

import com.recruitment.ai.entity.PromptTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTemplateVersionRepository extends JpaRepository<PromptTemplateVersion, UUID> {

    Optional<PromptTemplateVersion> findByTemplateCodeAndActiveTrue(String templateCode);

    boolean existsByTemplateCodeAndVersionNumber(String templateCode, Integer versionNumber);

}
