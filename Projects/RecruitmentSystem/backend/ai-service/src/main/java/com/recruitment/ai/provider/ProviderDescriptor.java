package com.recruitment.ai.provider;

public record ProviderDescriptor(
        String providerName,
        String implementation,
        boolean available
) {
}
