package com.recruitment.application.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClientDto {

    private UUID id;

    private UUID userId;

    private String displayName;

    private String headline;

    private String summary;

    private String contactEmail;

    private String contactPhone;

    private String rawJsonData;

}
