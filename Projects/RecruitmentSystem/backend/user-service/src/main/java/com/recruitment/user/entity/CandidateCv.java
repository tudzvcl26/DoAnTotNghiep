package com.recruitment.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "candidate_cvs", indexes = {
        @Index(name = "idx_candidate_cvs_candidate_updated", columnList = "candidate_id,updated_at")
})
public class CandidateCv extends BaseEntity {

    @Column(name = "candidate_id", nullable = false, updatable = false)
    private UUID candidateId;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank
    @Size(max = 40)
    @Column(name = "template_id", nullable = false, length = 40)
    private String templateId;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String language;

    @NotBlank
    @Column(name = "content_json", nullable = false, columnDefinition = "TEXT")
    private String contentJson;
}
