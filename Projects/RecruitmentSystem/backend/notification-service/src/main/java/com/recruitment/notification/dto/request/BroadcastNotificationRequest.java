package com.recruitment.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BroadcastNotificationRequest {

    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must not exceed 200 characters.")
    private String title;

    @NotBlank(message = "Content is required.")
    @Size(max = 4000, message = "Content must not exceed 4000 characters.")
    private String content;

    private Map<String, Object> payload;

}
