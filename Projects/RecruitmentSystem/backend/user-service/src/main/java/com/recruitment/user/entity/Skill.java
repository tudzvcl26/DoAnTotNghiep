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

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "skills", indexes = {
        @Index(name = "idx_skills_normalized_key", columnList = "normalized_skill_key")
})
public class Skill extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(name = "normalized_skill_key", nullable = false, unique = true, length = 150)
    private String normalizedSkillKey;

    @NotBlank
    @Size(max = 150)
    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

}
