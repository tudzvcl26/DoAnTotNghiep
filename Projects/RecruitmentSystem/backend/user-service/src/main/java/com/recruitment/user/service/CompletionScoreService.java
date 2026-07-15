package com.recruitment.user.service;

import com.recruitment.user.entity.Profile;
import com.recruitment.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompletionScoreService {

    private final ProfileRepository profileRepository;

    public void recalculate(Profile profile) {

        profile.setCompletionScore(calculate(profile));
        profile.setCompletionCalculatedAt(LocalDateTime.now());

        profileRepository.save(profile);

    }

    public void recalculate(UUID userId) {

        Profile profile = profileRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow();

        recalculate(profile);

    }

    public Integer calculate(Profile profile) {

        int score = 0;

        if (hasText(profile.getDisplayName()))
            score += 5;

        if (hasText(profile.getHeadline()))
            score += 10;

        if (hasText(profile.getSummary()))
            score += 10;

        if (profile.getCareerObjective() != null)
            score += 5;

        if (profile.getCandidatePreference() != null)
            score += 5;

        if (profile.getEducations() != null && !profile.getEducations().isEmpty())
            score += 15;

        if (profile.getExperiences() != null && !profile.getExperiences().isEmpty())
            score += 20;

        if (profile.getUserSkills() != null && !profile.getUserSkills().isEmpty())
            score += 15;

        if (profile.getUserLanguages() != null && !profile.getUserLanguages().isEmpty())
            score += 10;

        if (profile.getSocialLinks() != null && !profile.getSocialLinks().isEmpty())
            score += 5;

        if (profile.getAssets() != null && !profile.getAssets().isEmpty())
            score += 15;

        return Math.min(score, 100);

    }

    private boolean hasText(String value) {

        return value != null && !value.isBlank();

    }

}