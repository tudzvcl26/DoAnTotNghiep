package com.recruitment.application.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

}
