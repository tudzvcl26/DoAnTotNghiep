package com.recruitment.ai.repository;

import com.recruitment.ai.entity.ModelDeployment;
import com.recruitment.ai.entity.enums.ModelCapability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelDeploymentRepository extends JpaRepository<ModelDeployment, UUID> {

    List<ModelDeployment> findByEnabledTrueOrderByProviderNameAscModelNameAsc();

    Optional<ModelDeployment> findByCapabilityAndEnabledTrueAndDefaultForCapabilityTrue(
            ModelCapability capability
    );

}
