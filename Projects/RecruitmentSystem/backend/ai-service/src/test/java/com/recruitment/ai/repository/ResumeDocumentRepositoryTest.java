package com.recruitment.ai.repository;

import com.recruitment.ai.entity.ResumeDocument;
import com.recruitment.ai.entity.enums.ResumeDocumentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ResumeDocumentRepositoryTest {

    @Autowired
    private ResumeDocumentRepository repository;

    @Test
    void filtersResumeDocumentsByOwner() {
        UUID owner = UUID.randomUUID();
        ResumeDocument document = new ResumeDocument();
        document.setOwnerUserId(owner);
        document.setBucketName("test-bucket");
        document.setObjectKey(owner + "/resumes/test/resume.txt");
        document.setOriginalFilename("resume.txt");
        document.setContentType("text/plain");
        document.setFileSize(100L);
        document.setChecksumSha256("a".repeat(64));
        document.setExtractedText("Java Spring Boot");
        document.setStatus(ResumeDocumentStatus.READY);
        document.setExtractionDurationMs(4L);
        document.setUploadTime(LocalDateTime.now());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        repository.saveAndFlush(document);

        assertThat(repository.findByOwnerUserId(owner, PageRequest.of(0, 10))).hasSize(1);
        assertThat(repository.findByOwnerUserId(UUID.randomUUID(), PageRequest.of(0, 10))).isEmpty();
        assertThat(repository.findByIdAndOwnerUserId(document.getId(), owner)).isPresent();
    }
}
