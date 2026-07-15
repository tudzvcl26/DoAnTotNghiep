package com.recruitment.user.repository;

import com.recruitment.user.entity.Profile;
import com.recruitment.user.entity.ProfileStatus;
import com.recruitment.user.entity.ProfileVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID>,
        JpaSpecificationExecutor<Profile> {

    Optional<Profile> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    Page<Profile> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Profile> findByProfileVisibilityAndProfileStatusAndDeletedAtIsNull(
            ProfileVisibility visibility,
            ProfileStatus status,
            Pageable pageable
    );

}