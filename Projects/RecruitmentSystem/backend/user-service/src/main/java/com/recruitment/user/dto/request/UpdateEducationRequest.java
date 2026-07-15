package com.recruitment.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateEducationRequest extends CreateEducationRequest { private Long version; }
