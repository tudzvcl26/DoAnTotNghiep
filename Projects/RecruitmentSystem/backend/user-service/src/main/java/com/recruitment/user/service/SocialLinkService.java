package com.recruitment.user.service;

import com.recruitment.user.dto.request.CreateSocialLinkRequest;
import com.recruitment.user.dto.request.UpdateSocialLinkRequest;
import com.recruitment.user.dto.response.SocialLinkResponse;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.entity.SocialLink;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.mapper.SocialLinkMapper;
import com.recruitment.user.repository.SocialLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialLinkService {

    private final SocialLinkRepository repository;
    private final SocialLinkMapper mapper;
    private final ProfileService profileService;
    private final CompletionScoreService completionScoreService;

    @Transactional(readOnly = true)
    public Page<SocialLinkResponse> getAll(
            UUID userId,
            Pageable pageable
    ) {

        profileService.assertProfileOwner(userId);

        Profile profile = profileService.getByUserId(userId);

        return repository
                .findByProfileIdAndDeletedAtIsNull(
                        profile.getId(),
                        pageable
                )
                .map(mapper::toResponse);

    }

    @Transactional(readOnly = true)
    public SocialLinkResponse getById(
            UUID socialLinkId
    ) {

        SocialLink entity = repository
                .findByIdAndDeletedAtIsNull(socialLinkId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Social link not found"
                        ));

        profileService.assertProfileOwner(entity.getProfile().getUserId());

        return mapper.toResponse(entity);

    }

    public SocialLinkResponse create(
            UUID userId,
            CreateSocialLinkRequest request
    ) {

        profileService.assertProfileOwner(userId);

        Profile profile = profileService.getByUserId(userId);

        if (repository.existsByProfileIdAndLinkTypeAndDeletedAtIsNull(
                profile.getId(),
                request.getLinkType()
        )) {

            throw new IllegalArgumentException(
                    "Social link already exists."
            );

        }

        SocialLink entity = mapper.toEntity(request);

        entity.setProfile(profile);

        SocialLink saved = repository.save(entity);

        completionScoreService.recalculate(profile);

        return mapper.toResponse(saved);

    }

    public SocialLinkResponse update(
            UUID socialLinkId,
            UpdateSocialLinkRequest request
    ) {

        SocialLink entity = repository
                .findByIdAndDeletedAtIsNull(socialLinkId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Social link not found"
                        ));

        profileService.assertProfileOwner(entity.getProfile().getUserId());

        if (request.getLinkType() != null &&
                !request.getLinkType().equals(entity.getLinkType())) {

            if (repository.existsByProfileIdAndLinkTypeAndDeletedAtIsNull(
                    entity.getProfile().getId(),
                    request.getLinkType()
            )) {

                throw new IllegalArgumentException(
                        "Social link already exists."
                );

            }

        }

        mapper.updateEntity(request, entity);

        SocialLink saved = repository.save(entity);

        completionScoreService.recalculate(saved.getProfile());

        return mapper.toResponse(saved);

    }

    public void delete(
            UUID socialLinkId
    ) {

        SocialLink entity = repository
                .findByIdAndDeletedAtIsNull(socialLinkId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Social link not found"
                        ));

        profileService.assertProfileOwner(entity.getProfile().getUserId());

        entity.setDeletedAt(LocalDateTime.now());

        repository.save(entity);

        completionScoreService.recalculate(entity.getProfile());

    }

}
