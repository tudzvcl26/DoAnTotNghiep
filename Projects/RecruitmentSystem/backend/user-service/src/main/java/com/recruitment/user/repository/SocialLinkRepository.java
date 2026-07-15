package com.recruitment.user.repository;

import com.recruitment.user.entity.SocialLink;
import com.recruitment.user.entity.SocialLinkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SocialLinkRepository extends JpaRepository<SocialLink, UUID>,
        JpaSpecificationExecutor<SocialLink> {

    Page<SocialLink> findByProfileIdAndDeletedAtIsNull(
            UUID profileId,
            Pageable pageable
    );

    Optional<SocialLink> findByIdAndDeletedAtIsNull(
            UUID id
    );

    Optional<SocialLink> findByProfileIdAndLinkTypeAndDeletedAtIsNull(
            UUID profileId,
            SocialLinkType linkType
    );

    boolean existsByProfileIdAndLinkTypeAndDeletedAtIsNull(
            UUID profileId,
            SocialLinkType linkType
    );

}