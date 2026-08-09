package com.recruitment.application.service;

import com.recruitment.application.entity.enums.ApplicationStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

@Component
public class ApplicationStatusTransitionPolicy {
    private static final Map<ApplicationStatus, EnumSet<ApplicationStatus>> EMPLOYER_TRANSITIONS =
            new EnumMap<>(ApplicationStatus.class);

    static {
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.APPLIED,
                EnumSet.of(ApplicationStatus.SCREENING, ApplicationStatus.REJECTED));
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.SCREENING,
                EnumSet.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED));
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.INTERVIEW,
                EnumSet.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED));
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.OFFER,
                EnumSet.of(ApplicationStatus.HIRED, ApplicationStatus.REJECTED));
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.HIRED, EnumSet.noneOf(ApplicationStatus.class));
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.REJECTED, EnumSet.noneOf(ApplicationStatus.class));
        EMPLOYER_TRANSITIONS.put(ApplicationStatus.WITHDRAWN, EnumSet.noneOf(ApplicationStatus.class));
    }

    public boolean canEmployerTransition(ApplicationStatus from, ApplicationStatus to) {
        return from != null && to != null && EMPLOYER_TRANSITIONS.getOrDefault(
                from, EnumSet.noneOf(ApplicationStatus.class)).contains(to);
    }

    public boolean canCandidateWithdraw(ApplicationStatus from) {
        return from == ApplicationStatus.APPLIED
                || from == ApplicationStatus.SCREENING
                || from == ApplicationStatus.INTERVIEW
                || from == ApplicationStatus.OFFER;
    }
}
