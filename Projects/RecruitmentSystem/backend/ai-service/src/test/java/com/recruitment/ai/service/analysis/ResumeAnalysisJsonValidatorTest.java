package com.recruitment.ai.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.MatchingProperties;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ResumeAnalysisJsonValidatorTest {
    private static final String EMPTY_FACTS = """
            {"fullName":"QA Candidate","email":null,"phone":null,"location":null,"linkedIn":null,"portfolio":null,
             "summary":null,"education":[],"experience":[],"projects":[],"skills":[],"technicalSkills":[],
             "softSkills":[],"languages":[],"certificates":[],"achievements":[],"keywords":[]}
            """;

    @Test void keepsOnlyTheEducationValueWrittenInTheSource() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        String generated = EMPTY_FACTS.replace("\"education\":[]",
                "\"education\":[\"Cử nhân CNTT, Trường Đại học Công nghệ thông tin, 2019-2023\"]");
        var result = validator.parseAndValidate(generated, "QA Candidate\nHọc vấn: Cử nhân CNTT 2019-2023");
        assertThat(result.path("education")).hasSize(1);
        assertThat(result.path("education").get(0).asText()).isEqualTo("Cử nhân CNTT 2019-2023");
        assertThat(result.toString()).doesNotContain("Trường Đại học");
    }

    @Test void doesNotTurnSchoolAndStudyDatesIntoLocation() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        String generated = EMPTY_FACTS.replace("\"location\":null",
                "\"location\":\"Đại học Mẫu, 2022-2026\"");
        var result = validator.parseAndValidate(generated,
                "Nguyễn Minh An\nSinh viên CNTT tại Đại học Mẫu, 2022-2026.");
        assertThat(result.path("location").isNull()).isTrue();

        var explicit = validator.parseAndValidate(
                EMPTY_FACTS.replace("\"location\":null", "\"location\":\"Hà Nội\""),
                "Nguyễn Minh An\nĐịa chỉ: Hà Nội");
        assertThat(explicit.path("location").asText()).isEqualTo("Hà Nội");
    }

    @Test void doesNotInferLanguageOrCertificateFromDocumentLanguageOrTechnologyNames() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        String generated = EMPTY_FACTS
                .replace("\"languages\":[]", "\"languages\":[\"Tiếng Việt - bản ngữ\",\"English - Fluent\"]")
                .replace("\"certificates\":[]", "\"certificates\":[\"AWS Certified Developer\"]");
        var result = validator.parseAndValidate(generated,
                "QA Candidate\nPhát triển tài liệu tiếng Việt bằng Java.\nKhông có chứng chỉ.\nNo AWS experience.");
        assertThat(result.path("languages")).isEmpty();
        assertThat(result.path("certificates")).isEmpty();
    }

    @Test void groupsOneEmploymentSectionWithBulletsIntoOneFact() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        String source = """
                QA Candidate
                KINH NGHIỆM
                07/2023 - 07/2026
                Software Developer
                Company X
                - Phát triển API
                - Thiết kế database
                - Viết unit test
                - Review mã nguồn
                - Hỗ trợ triển khai
                KỸ NĂNG
                Java
                """;
        String generated = EMPTY_FACTS.replace("\"experience\":[]",
                "\"experience\":[\"Phát triển API\",\"Thiết kế database\",\"Viết unit test\",\"Review mã nguồn\",\"Hỗ trợ triển khai\"]");
        var result = validator.parseAndValidate(generated, source);
        assertThat(result.path("experience")).hasSize(1);
        assertThat(result.path("experience").get(0).asText()).contains("Company X", "Phát triển API", "Hỗ trợ triển khai");
    }

    @Test void keepsExplicitProjectOutOfExperience() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        String generated = EMPTY_FACTS
                .replace("\"projects\":[]", "\"projects\":[]")
                .replace("\"experience\":[]", "\"experience\":[\"Hệ thống quản lý đơn hàng\"]");
        var result = validator.parseAndValidate(generated, "QA Candidate\nDự án: Hệ thống quản lý đơn hàng");
        assertThat(result.path("projects")).hasSize(1);
        assertThat(result.path("projects").get(0).asText()).isEqualTo("Hệ thống quản lý đơn hàng");
        assertThat(result.path("experience")).isEmpty();
    }

    @Test void preservesExplicitEmploymentDurationWhenModelOnlyReturnsDatesWithoutInferringFromEducation() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        String generated = """
                {"fullName":"QA","email":null,"phone":null,"location":null,"linkedIn":null,"portfolio":null,"summary":null,
                 "education":[],"experience":["07/2023-07/2026 Công ty Mẫu"],"projects":[],"skills":[],"technicalSkills":[],
                 "softSkills":[],"languages":[],"certificates":[],"achievements":[],"keywords":[]}
                """;
        var result = validator.parseAndValidate(generated, "QA\nKinh nghiệm: 3 năm, 07/2023-07/2026, Công ty Mẫu.\nHọc vấn: 4 năm CNTT.");
        assertThat(result.path("experience").toString()).contains("3 năm, 07/2023-07/2026, Công ty Mẫu.").doesNotContain("4 năm");
        assertThat(validator.parseAndValidate(result.toString(), "Kinh nghiệm: 3 năm, 07/2023-07/2026, Công ty Mẫu.").path("experience")).hasSize(1);
        assertThat(validator.parseAndValidate(generated, "Học vấn: 4 năm CNTT.").path("experience")).isEmpty();
    }
    @Test void preservesExplicitSourceProjectAndRecognizesBasicSkillWithoutInventingProficiency() {
        var properties = new MatchingProperties();
        properties.setTechnicalSkillCatalog(List.of("Java"));
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), properties);
        var result = validator.parseAndValidate("""
                {"fullName":"QA","email":null,"phone":null,"location":null,"linkedIn":null,"portfolio":null,"summary":null,
                 "education":[],"experience":["3 năm Java"],"projects":[],"skills":["Java cơ bản"],"technicalSkills":[],
                 "softSkills":[],"languages":[],"certificates":[],"achievements":[],"keywords":[]}
                """, "QA\nKỹ năng: Java cơ bản.\nDự án: hệ thống quản lý đơn hàng; phụ trách API tạo đơn.");
        assertThat(result.path("projects").get(0).asText()).isEqualTo("hệ thống quản lý đơn hàng; phụ trách API tạo đơn.");
        assertThat(result.path("technicalSkills").get(0).asText()).isEqualTo("Java cơ bản");
        assertThat(result.path("technicalSkills").toString()).doesNotContain("Spring");
    }
    @Test void removesInventedContactsAndNegatedSkillsBeforeScoring() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        var result = validator.parseAndValidate("""
                {"fullName":"Trần Quốc Bình","email":"fake@example.test","phone":"0123456789","location":"Hà Nội",
                 "linkedIn":"https://www.linkedin.com/in/tranquocbinh/","portfolio":"https://invented.test","summary":null,
                 "education":[],"experience":["3 năm"],"projects":[],"skills":["Java","AWS","NoSQL"],
                 "technicalSkills":["Git","AWS (không làm việc)","Kafka","Kubernetes"],"softSkills":[],
                 "languages":[],"certificates":[],"achievements":[],"keywords":["Java","AWS"]}
                """, "Trần Quốc Bình\n3 năm phát triển Java, Git, NoSQL.\nChưa làm việc với AWS, Kafka hoặc Kubernetes.");
        for (String field : List.of("email", "phone", "location", "linkedIn", "portfolio")) assertThat(result.path(field).isNull()).isTrue();
        assertThat(result.path("technicalSkills").toString()).contains("Git").doesNotContain("AWS", "Kafka", "Kubernetes");
        assertThat(result.path("skills").toString()).contains("Java", "NoSQL").doesNotContain("AWS");
        assertThat(result.path("keywords").toString()).doesNotContain("AWS");
        assertThat(result.path("experience").get(0).asText()).isEqualTo("3 năm");
    }
    @Test void removesInventedEducationLanguagesAndSkillsFromTheRuntimeRegressionCv() {
        var properties = new MatchingProperties();
        properties.setTechnicalSkillCatalog(List.of("Java", "Spring Boot", "PostgreSQL", "JUnit", "Git", "Docker"));
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), properties);
        String source = """
                Trần Quốc Bình
                Java Backend Developer
                Kinh nghiệm: 3 năm, 07/2023-07/2026, Công ty Mẫu.
                Phát triển REST API bằng Java, Spring Boot, PostgreSQL; viết unit test JUnit; quản lý mã bằng Git.
                Dự án: hệ thống quản lý đơn hàng, phụ trách API tạo đơn và kiểm tra dữ liệu.
                Học vấn: Cử nhân CNTT 2019-2023.
                Chưa làm việc với AWS, Kafka hoặc Kubernetes. Không có chứng chỉ.
                """;
        var result = validator.parseAndValidate("""
                {"fullName":"Trần Quốc Bình","email":null,"phone":null,"location":null,"linkedIn":null,"portfolio":null,
                 "summary":"Java Backend Developer","education":["Cử nhân CNTT, Trường Đại học Công nghệ thông tin, 2019-2023"],
                 "experience":["3 năm tại Công ty Mẫu","07/2023-07/2026"],
                 "projects":["Hệ thống quản lý đơn hàng, phụ trách API tạo đơn và kiểm tra dữ liệu"],
                 "skills":["Java","Spring Boot","PostgreSQL","JUnit","Git","Docker","AWS"],
                 "technicalSkills":["Java","Docker"],"softSkills":[],"languages":["Tiếng Việt","Tiếng Anh"],
                 "certificates":["AWS Certified Developer"],"achievements":[],"keywords":["Java","Docker","AWS"]}
                """, source);
        assertThat(result.path("education")).hasSize(1);
        assertThat(result.path("education").get(0).asText()).isEqualTo("Cử nhân CNTT 2019-2023.");
        assertThat(result.path("languages")).isEmpty();
        assertThat(result.path("certificates")).isEmpty();
        assertThat(result.path("skills").toString()).contains("Java", "Spring Boot", "PostgreSQL", "JUnit", "Git")
                .doesNotContain("Docker", "AWS");
        assertThat(result.path("technicalSkills").toString()).contains("Java").doesNotContain("Docker");
        assertThat(result.path("projects").toString()).contains("hệ thống quản lý đơn hàng");
    }
    @Test void preservesAnExplicitSourceNameAndClearsVietnameseAbsencePlaceholders() {
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), new MatchingProperties());
        var result = validator.parseAndValidate("""
                {"fullName":"Nguyễn Văn","email":null,"phone":"Không có thông tin trong CV","location":null,
                 "linkedIn":"Không có liên kết LinkedIn trong CV","portfolio":"Không có trang web cá nhân trong CV","summary":null,
                 "education":[],"experience":[],"projects":[],"skills":[],"technicalSkills":[],"softSkills":[],
                 "languages":[],"certificates":[],"achievements":[],"keywords":[]}
                """, "Nguyễn Văn An\nKỹ sư Java\n3 năm kinh nghiệm");
        assertThat(result.path("fullName").asText()).isEqualTo("Nguyễn Văn An");
        for (String field : List.of("phone", "linkedIn", "portfolio")) assertThat(result.path(field).isNull()).isTrue();
        assertThat(result.path("experience")).isEmpty();
    }
    @Test void normalizesPlaceholderStringsAndKnownGeneralSkillsWithoutInventingFacts() {
        var properties = new MatchingProperties();
        properties.setTechnicalSkillCatalog(List.of("Java", "REST"));
        var validator = new ResumeAnalysisJsonValidator(new ObjectMapper(), properties);
        var result = validator.parseAndValidate("""
                {"fullName":"Nguyễn Văn An","email":"an@example.test","phone":"null","location":null,
                 "linkedIn":"undefined","portfolio":"Not provided in CV","summary":"Ứng viên kiểm thử",
                 "education":[],"experience":[],"projects":[],"skills":["Java","java","REST API","Giao tiếp"],
                 "technicalSkills":[],"softSkills":[],"languages":[],"certificates":["null",null,"Không có kinh nghiệm AWS. Không có chứng chỉ chuyên môn.","No professional certificates"],
                 "achievements":[],"keywords":[]}
                """);
        assertThat(result.path("phone").isNull()).isTrue();
        assertThat(result.path("linkedIn").isNull()).isTrue();
        assertThat(result.path("portfolio").isNull()).isTrue();
        assertThat(result.path("certificates")).isEmpty();
        assertThat(result.path("technicalSkills")).hasSize(2);
        assertThat(result.path("technicalSkills").toString()).contains("Java", "REST API").doesNotContain("AWS", "Giao tiếp");
        assertThat(result.path("experience")).isEmpty();
    }
}
