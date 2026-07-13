package com.recruitment.auth.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

}