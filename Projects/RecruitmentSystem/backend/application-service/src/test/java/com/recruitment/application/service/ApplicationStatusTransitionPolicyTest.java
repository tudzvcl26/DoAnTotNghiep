package com.recruitment.application.service;

import com.recruitment.application.entity.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTransitionPolicyTest {
    private final ApplicationStatusTransitionPolicy policy = new ApplicationStatusTransitionPolicy();

    @Test
    void employerTransitionMatrixIsExplicitAndComplete() {
        Set<String> valid = Set.of(
                "APPLIED->SCREENING", "APPLIED->REJECTED",
                "SCREENING->INTERVIEW", "SCREENING->REJECTED",
                "INTERVIEW->OFFER", "INTERVIEW->REJECTED",
                "OFFER->HIRED", "OFFER->REJECTED"
        );

        for (ApplicationStatus from : ApplicationStatus.values()) {
            for (ApplicationStatus to : ApplicationStatus.values()) {
                assertThat(policy.canEmployerTransition(from, to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(valid.contains(from + "->" + to));
            }
        }
    }

    @Test
    void candidateCanWithdrawOnlyBeforeTerminalDecision() {
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.APPLIED)).isTrue();
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.SCREENING)).isTrue();
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.INTERVIEW)).isTrue();
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.OFFER)).isTrue();
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.HIRED)).isFalse();
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.REJECTED)).isFalse();
        assertThat(policy.canCandidateWithdraw(ApplicationStatus.WITHDRAWN)).isFalse();
    }
}
