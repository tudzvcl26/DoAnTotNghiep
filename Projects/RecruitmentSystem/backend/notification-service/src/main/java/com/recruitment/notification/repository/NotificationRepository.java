package com.recruitment.notification.repository;

import com.recruitment.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query(value = """
            select notification from Notification notification
            where (
                    (notification.audienceType = com.recruitment.notification.entity.enums.NotificationAudienceType.ALL_USERS
                     and not exists (
                        select broadcastState from NotificationUserState broadcastState
                        where broadcastState.notification = notification
                          and broadcastState.userId = :userId
                          and broadcastState.deletedAt is not null
                     ))
                    or exists (
                        select personalState from NotificationUserState personalState
                        where personalState.notification = notification
                          and personalState.userId = :userId
                          and personalState.deletedAt is null
                    )
                  )
              and (:eventType is null or notification.eventType = :eventType)
              and (:query = '' or lower(notification.title) like lower(concat('%', :query, '%'))
                   or lower(notification.content) like lower(concat('%', :query, '%')))
              and (:read is null
                   or (:read = true and exists (
                        select readState from NotificationUserState readState
                        where readState.notification = notification
                          and readState.userId = :userId
                          and readState.deletedAt is null
                          and readState.readAt is not null
                   ))
                   or (:read = false and (
                        (notification.audienceType = com.recruitment.notification.entity.enums.NotificationAudienceType.ALL_USERS
                         and not exists (
                            select broadcastReadState from NotificationUserState broadcastReadState
                            where broadcastReadState.notification = notification
                              and broadcastReadState.userId = :userId
                              and broadcastReadState.deletedAt is null
                              and broadcastReadState.readAt is not null
                         ))
                        or exists (
                            select unreadState from NotificationUserState unreadState
                            where unreadState.notification = notification
                              and unreadState.userId = :userId
                              and unreadState.deletedAt is null
                              and unreadState.readAt is null
                        )
                   )))
            """, countQuery = """
            select count(notification) from Notification notification
            where (
                    (notification.audienceType = com.recruitment.notification.entity.enums.NotificationAudienceType.ALL_USERS
                     and not exists (
                        select broadcastState from NotificationUserState broadcastState
                        where broadcastState.notification = notification
                          and broadcastState.userId = :userId
                          and broadcastState.deletedAt is not null
                     ))
                    or exists (
                        select personalState from NotificationUserState personalState
                        where personalState.notification = notification
                          and personalState.userId = :userId
                          and personalState.deletedAt is null
                    )
                  )
              and (:eventType is null or notification.eventType = :eventType)
              and (:query = '' or lower(notification.title) like lower(concat('%', :query, '%'))
                   or lower(notification.content) like lower(concat('%', :query, '%')))
              and (:read is null
                   or (:read = true and exists (
                        select readState from NotificationUserState readState
                        where readState.notification = notification
                          and readState.userId = :userId
                          and readState.deletedAt is null
                          and readState.readAt is not null
                   ))
                   or (:read = false and (
                        (notification.audienceType = com.recruitment.notification.entity.enums.NotificationAudienceType.ALL_USERS
                         and not exists (
                            select broadcastReadState from NotificationUserState broadcastReadState
                            where broadcastReadState.notification = notification
                              and broadcastReadState.userId = :userId
                              and broadcastReadState.deletedAt is null
                              and broadcastReadState.readAt is not null
                         ))
                        or exists (
                            select unreadState from NotificationUserState unreadState
                            where unreadState.notification = notification
                              and unreadState.userId = :userId
                              and unreadState.deletedAt is null
                              and unreadState.readAt is null
                        )
                   )))
            """)
    Page<Notification> searchVisible(
            @Param("userId") UUID userId,
            @Param("eventType") com.recruitment.notification.entity.enums.NotificationEventType eventType,
            @Param("read") Boolean read,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("""
            select count(notification) from Notification notification
            where (
                    (notification.audienceType = com.recruitment.notification.entity.enums.NotificationAudienceType.ALL_USERS
                     and not exists (
                        select broadcastState from NotificationUserState broadcastState
                        where broadcastState.notification = notification
                          and broadcastState.userId = :userId
                          and broadcastState.deletedAt is not null
                     ))
                    or exists (
                        select personalState from NotificationUserState personalState
                        where personalState.notification = notification
                          and personalState.userId = :userId
                          and personalState.deletedAt is null
                          and personalState.readAt is null
                    )
                  )
              and not exists (
                    select readState from NotificationUserState readState
                    where readState.notification = notification
                      and readState.userId = :userId
                      and readState.deletedAt is null
                      and readState.readAt is not null
              )
            """)
    long countVisibleUnread(@Param("userId") UUID userId);

}
