package com.recruitment.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.user.dto.cv.CvDocument;
import com.recruitment.user.dto.cv.CvTemplateCatalog;
import com.recruitment.user.dto.request.CreateCvFromProfileRequest;
import com.recruitment.user.dto.request.SaveCandidateCvRequest;
import com.recruitment.user.dto.response.CandidateCvResponse;
import com.recruitment.user.entity.CandidateCv;
import com.recruitment.user.entity.Profile;
import com.recruitment.user.exception.ResourceNotFoundException;
import com.recruitment.user.repository.CandidateCvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateCvService {

    private final CandidateCvRepository repository;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;
    private final CvPdfService pdfService;

    @Transactional(readOnly = true)
    public List<CandidateCvResponse> list(UUID candidateId) {
        return repository.findAllByCandidateIdAndDeletedAtIsNullOrderByUpdatedAtDesc(candidateId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CandidateCvResponse get(UUID candidateId, UUID cvId) {
        return toResponse(findOwned(candidateId, cvId));
    }

    public CandidateCvResponse create(UUID candidateId, SaveCandidateCvRequest request) {
        CandidateCv cv = CandidateCv.builder()
                .candidateId(candidateId)
                .title(request.title().trim())
                .templateId(request.templateId())
                .language(request.language())
                .contentJson(write(normalize(request.content())))
                .createdBy(candidateId)
                .updatedBy(candidateId)
                .build();
        return toResponse(repository.saveAndFlush(cv));
    }

    public CandidateCvResponse createFromProfile(UUID candidateId, String authenticatedEmail,
                                                  CreateCvFromProfileRequest request) {
        Profile profile = profileService.getByUserId(candidateId);
        String website = profile.getSocialLinks().stream()
                .map(link -> link.getUrl()).filter(Objects::nonNull).findFirst().orElse("");
        CvDocument content = new CvDocument(
                new CvDocument.CvPersonalInfo(
                        text(profile.getDisplayName()), text(profile.getHeadline()),
                        firstNonBlank(profile.getContactEmail(), authenticatedEmail),
                        text(profile.getContactPhone()), location(profile), website),
                firstNonBlank(profile.getSummary(), profile.getCareerObjective() == null
                        ? null : profile.getCareerObjective().getObjectiveText()),
                profile.getExperiences().stream().map(item -> new CvDocument.CvExperience(
                        text(item.getJobTitle()), text(item.getEmployerName()), date(item.getStartDate()),
                        Boolean.TRUE.equals(item.getCurrent()) ? "Hiện tại" : date(item.getEndDate()),
                        join(item.getDescription(), item.getAchievements()))).toList(),
                profile.getEducations().stream().map(item -> new CvDocument.CvEducation(
                        text(item.getInstitutionName()), join(item.getQualification(), item.getFieldOfStudy()),
                        date(item.getStartDate()), date(item.getEndDate()), text(item.getDescription()))).toList(),
                profile.getUserSkills().stream().map(item -> item.getSkill().getDisplayName()).toList(),
                List.of(),
                profile.getCertificates().stream().map(item -> new CvDocument.CvCertification(
                        text(item.getCertificateName()), text(item.getIssuerName()), date(item.getIssueDate()))).toList(),
                List.of(), List.of(), CvTemplateCatalog.design(request.templateId()), List.of());
        return create(candidateId, new SaveCandidateCvRequest(
                request.title(), request.templateId(), "vi", content));
    }

    public CandidateCvResponse update(UUID candidateId, UUID cvId, SaveCandidateCvRequest request) {
        CandidateCv cv = findOwned(candidateId, cvId);
        cv.setTitle(request.title().trim());
        cv.setTemplateId(request.templateId());
        cv.setLanguage(request.language());
        cv.setContentJson(write(normalize(request.content())));
        cv.setUpdatedBy(candidateId);
        return toResponse(repository.saveAndFlush(cv));
    }

    public void delete(UUID candidateId, UUID cvId) {
        CandidateCv cv = findOwned(candidateId, cvId);
        cv.setDeletedAt(LocalDateTime.now());
        cv.setDeletedBy(candidateId);
        repository.save(cv);
    }

    @Transactional(readOnly = true)
    public byte[] download(UUID candidateId, UUID cvId) {
        CandidateCv cv = findOwned(candidateId, cvId);
        return pdfService.render(cv.getTemplateId(), cv.getLanguage(), read(cv.getContentJson()));
    }

    private CandidateCv findOwned(UUID candidateId, UUID cvId) {
        CandidateCv cv = repository.findByIdAndDeletedAtIsNull(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV not found"));
        if (!cv.getCandidateId().equals(candidateId)) {
            throw new ResourceNotFoundException("CV not found");
        }
        return cv;
    }

    private CandidateCvResponse toResponse(CandidateCv cv) {
        return new CandidateCvResponse(cv.getId(), cv.getTitle(), cv.getTemplateId(), cv.getLanguage(),
                read(cv.getContentJson()), cv.getVersion(), cv.getCreatedAt(), cv.getUpdatedAt());
    }

    private String write(CvDocument content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid CV content", exception);
        }
    }

    private CvDocument read(String value) {
        try {
            return normalize(objectMapper.readValue(value, CvDocument.class));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored CV content is invalid", exception);
        }
    }

    private CvDocument normalize(CvDocument value) {
        if (value == null) return CvDocument.empty();
        List<CvDocument.CvCustomSection> customSections = list(value.customSections()).stream()
                .filter(Objects::nonNull)
                .map(section -> new CvDocument.CvCustomSection(text(section.id()), text(section.title()),
                        list(section.items()), section.visible()))
                .toList();
        return new CvDocument(value.personalInfo() == null ? CvDocument.CvPersonalInfo.empty() : value.personalInfo(),
                text(value.summary()), list(value.experiences()), list(value.education()), list(value.skills()),
                list(value.projects()), list(value.certifications()), list(value.awards()), list(value.activities()),
                normalizeDesign(value.designConfig(), customSections), customSections);
    }

    private CvDocument.CvDesignConfig normalizeDesign(CvDocument.CvDesignConfig value,
                                                       List<CvDocument.CvCustomSection> customSections) {
        CvDocument.CvDesignConfig defaults = CvDocument.CvDesignConfig.defaults();
        if (value == null) value = defaults;
        List<String> supported = new ArrayList<>(defaults.sectionOrder());
        customSections.forEach(section -> supported.add("custom:" + section.id()));
        Set<String> supportedSet = Set.copyOf(supported);
        List<String> order = new ArrayList<>();
        list(value.sectionOrder()).stream().filter(supportedSet::contains).distinct().forEach(order::add);
        supported.stream().filter(section -> !order.contains(section)).forEach(order::add);
        Map<String, Boolean> visibility = new LinkedHashMap<>();
        if (value.sectionVisibility() != null) value.sectionVisibility().forEach((key, visible) -> {
            if (supportedSet.contains(key) && visible != null) visibility.put(key, visible);
        });
        CvDocument.CvThemeConfig theme = value.theme() == null ? defaults.theme() : value.theme();
        double scale = value.fontScale() < 0.85 || value.fontScale() > 1.15 ? defaults.fontScale() : value.fontScale();
        return new CvDocument.CvDesignConfig(
                firstNonBlank(value.fontFamily(), defaults.fontFamily()), scale, theme,
                firstNonBlank(value.density(), defaults.density()), firstNonBlank(value.layout(), defaults.layout()),
                List.copyOf(order), Map.copyOf(visibility));
    }

    private static <T> List<T> list(List<T> value) { return value == null ? List.of() : value; }
    private static String text(String value) { return value == null ? "" : value; }
    private static String date(LocalDate value) { return value == null ? "" : value.toString(); }
    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : text(second);
    }
    private static String join(String first, String second) {
        if (first == null || first.isBlank()) return text(second);
        if (second == null || second.isBlank()) return first;
        return first + " · " + second;
    }
    private static String location(Profile profile) {
        return join(profile.getDistrictName(), profile.getCityName());
    }
}
