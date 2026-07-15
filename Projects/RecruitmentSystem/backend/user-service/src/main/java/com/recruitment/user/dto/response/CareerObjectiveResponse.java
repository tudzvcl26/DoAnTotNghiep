package com.recruitment.user.dto.response;
import com.recruitment.user.entity.AvailabilityStatus; import lombok.Builder; import lombok.Getter; import java.util.UUID;
@Getter @Builder public class CareerObjectiveResponse { private UUID id; private String objectiveText; private String targetSeniority; private AvailabilityStatus availabilityStatus; private Long version; }
