package com.recruitment.notification.service;

import com.recruitment.notification.entity.Notification;

import java.util.UUID;

public interface EmailSender {

    void send(UUID recipientUserId, Notification notification);

}
