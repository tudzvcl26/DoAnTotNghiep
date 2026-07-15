package com.recruitment.user.dto.request;

import com.recruitment.user.entity.ProfileAssetKind;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProfileAssetRequest {

    @NotNull
    private ProfileAssetKind assetKind;

}