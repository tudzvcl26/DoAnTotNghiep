package com.recruitment.ai.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.matching.model.JobRequirements;
import com.recruitment.ai.matching.model.JobSnapshot;
import com.recruitment.ai.matching.model.MatchingContext;
import com.recruitment.ai.matching.scorer.CertificateScorer;
import com.recruitment.ai.matching.scorer.EducationScorer;
import com.recruitment.ai.matching.scorer.ExperienceScorer;
import com.recruitment.ai.matching.scorer.KeywordScorer;
import com.recruitment.ai.matching.scorer.LanguageScorer;
import com.recruitment.ai.matching.scorer.ProjectScorer;
import com.recruitment.ai.matching.scorer.SkillScorer;
import com.recruitment.ai.matching.scorer.SoftSkillScorer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingScorersTest {

    private MatchingContext context;

    @BeforeEach
    void setUp() throws Exception {
        JsonNode facts = new ObjectMapper().readTree("""
                {"technicalSkills":["Java","Docker"],"skills":[],"softSkills":["Teamwork"],
                 "experience":[{"duration":"3 years"}],"education":[{"degree":"Bachelor"}],
                 "projects":[{"description":"Java service"}],"certificates":[],
                 "languages":["English"],"keywords":["Java","Docker"]}
                """);
        JobRequirements requirements = new JobRequirements(
                List.of("Java", "Spring Boot"), List.of("Docker"), List.of("Teamwork"),
                List.of("English"), List.of("java", "spring", "docker"), 5, true, true);
        JobSnapshot job = new JobSnapshot(UUID.randomUUID(), "Java Engineer", "", "", "",
                "SENIOR", "PUBLISHED", true, UUID.randomUUID(), UUID.randomUUID());
        context = new MatchingContext(facts, job, requirements);
    }

    @Test void scoresTechnicalSkills() {
        var result = new SkillScorer().score(context, 40);
        assertThat(result.actualScore()).isEqualTo(24);
        assertThat(result.reason()).contains("Kỹ năng bắt buộc phù hợp").doesNotContain("Required skills");
    }

    @Test void scoresExperienceAgainstThreshold() {
        var result = new ExperienceScorer().score(context, 20);
        assertThat(result.actualScore()).isEqualTo(12);
        assertThat(result.reason()).contains("Kinh nghiệm nhận diện được").doesNotContain("Detected experience");
    }

    @Test void scoresEducationRequirement() {
        assertThat(new EducationScorer().score(context, 10).actualScore()).isEqualTo(10);
    }

    @Test void neverInventsYearsFromExperienceEntryCount() throws Exception {
        JsonNode facts = new ObjectMapper().readTree("""
                {"experience":[{"position":"Intern"},{"position":"Volunteer"}]}
                """);
        var result = new ExperienceScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 20);
        assertThat(result.actualScore()).isZero();
        assertThat(result.reason()).contains("Chưa đủ dữ liệu");
    }

    @Test void twoJobsWithoutDurationRemainUnknownRatherThanTwoYears() throws Exception {
        JsonNode facts = new ObjectMapper().readTree("""
                {"experience":[{"position":"Developer","company":"A"},{"position":"Engineer","company":"B"}]}
                """);
        var result = new ExperienceScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 20);
        assertThat(result.actualScore()).isZero();
        assertThat(result.reason()).contains("Chưa đủ dữ liệu").doesNotContain("2 năm");
    }

    @Test void recognizesVietnameseExperienceWithoutCountingEntries() throws Exception {
        JsonNode facts = new ObjectMapper().readTree("""
                {"experience":[{"description":"3 năm phát triển Java"}]}
                """);
        assertThat(new ExperienceScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 20).actualScore()).isEqualTo(12);
    }

    @Test void neverCountsVietnameseCalendarMonthsAsYearsOfExperience() throws Exception {
        var mapper = new ObjectMapper();
        var facts = mapper.readTree("""
                {"summary":"Frontend Developer với kinh nghiệm 2 năm",
                 "experience":["Tháng 7 năm 2024 - Tháng 7 năm 2026"]}
                """);
        var result = new ExperienceScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 20);
        assertThat(result.actualScore()).isEqualTo(8);
        assertThat(result.reason()).contains("2 năm").doesNotContain("7 năm");
        var datesOnly = mapper.readTree("{\"experience\":[\"thang 12 nam 2023 den thang 7 nam 2026\"]}");
        assertThat(new ExperienceScorer().score(new MatchingContext(datesOnly, context.job(), context.requirements()), 20).actualScore()).isZero();
    }

    @Test void recognizesExplicitExperienceInSummaryButNotUnrelatedFutureYears() throws Exception {
        var mapper = new ObjectMapper();
        var facts = mapper.readTree("{\"summary\":\"Backend engineer with 5 years of experience\",\"experience\":[\"2021-2026\"]}");
        assertThat(new ExperienceScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 20).actualScore()).isEqualTo(20);
        var future = mapper.readTree("{\"summary\":\"Plan to finish university in 5 years\",\"experience\":[]}");
        assertThat(new ExperienceScorer().score(new MatchingContext(future, context.job(), context.requirements()), 20).actualScore()).isZero();
    }

    @Test void equivalentSkillAliasesAndDuplicatesDoNotChangeTheScore() throws Exception {
        JsonNode facts = new ObjectMapper().readTree("""
                {"technicalSkills":["REST API", "Java", "Java"],"skills":["REST APIs"]}
                """);
        var requirements = new JobRequirements(List.of("REST", "Java", "java"), List.of("Java", "REST API"), List.of(), List.of(), List.of(), 0, false, false);
        var result = new SkillScorer().score(new MatchingContext(facts, context.job(), requirements), 40);
        assertThat(result.actualScore()).isEqualTo(40);
        assertThat(result.reason()).contains("2/2", "0/0");
    }

    @Test void scoresProjectTechnologyCoverage() {
        assertThat(new ProjectScorer().score(context, 10).actualScore()).isEqualTo(3);
    }

    @Test void scoresMissingRequiredCertificate() {
        assertThat(new CertificateScorer().score(context, 5).actualScore()).isZero();
    }

    @Test void explicitAbsenceNeverCountsAsCertificationIncludingLegacyAnalyses() throws Exception {
        for (String evidence : List.of("Không có kinh nghiệm AWS. Không có chứng chỉ chuyên môn.",
                "No professional certificates", "Chưa có chứng chỉ", "N/A", "None")) {
            var facts = new ObjectMapper().createObjectNode();
            facts.putArray("certificates").add(evidence);
            assertThat(new CertificateScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 5).actualScore())
                    .as(evidence).isZero();
        }
        var facts = new ObjectMapper().readTree("{\"certificates\":[\"NoSQL Database Certification\"]}");
        assertThat(new CertificateScorer().score(new MatchingContext(facts, context.job(), context.requirements()), 5).actualScore()).isEqualTo(5);
        var absentObject = new ObjectMapper().readTree("{\"certificates\":[{\"name\":\"Không có\",\"issuer\":\"Oracle\"}]}");
        assertThat(new CertificateScorer().score(new MatchingContext(absentObject, context.job(), context.requirements()), 5).actualScore()).isZero();
        var presentObject = new ObjectMapper().readTree("{\"certificates\":[{\"certificateName\":\"Oracle Java\",\"issuer\":\"Oracle\"}]}");
        assertThat(new CertificateScorer().score(new MatchingContext(presentObject, context.job(), context.requirements()), 5).actualScore()).isEqualTo(5);
    }

    @Test void scoresLanguages() {
        assertThat(new LanguageScorer().score(context, 5).actualScore()).isEqualTo(5);
    }

    @Test void scoresSoftSkills() {
        assertThat(new SoftSkillScorer().score(context, 5).actualScore()).isEqualTo(5);
    }

    @Test void scoresKeywords() {
        assertThat(new KeywordScorer().score(context, 5).actualScore()).isEqualTo(3);
    }
}
