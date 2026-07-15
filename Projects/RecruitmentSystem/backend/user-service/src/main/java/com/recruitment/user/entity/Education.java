package com.recruitment.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "educations", indexes = {
        @Index(name = "idx_educations_profile_id", columnList = "profile_id"),
        @Index(name = "idx_educations_profile_start_date", columnList = "profile_id,start_date")
})
public class Education extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @NotBlank
    @Size(max = 255)
    @Column(name = "institution_name", nullable = false, length = 255)
    private String institutionName;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String qualification;

    @Size(max = 200)
    @Column(name = "field_of_study", length = 200)
    private String fieldOfStudy;

    @PastOrPresent
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Size(max = 50)
    @Column(length = 50)
    private String grade;

    @Column(columnDefinition = "TEXT")
    private String description;

}
