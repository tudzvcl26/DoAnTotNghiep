package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AssistantSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssistantSessionRepository extends JpaRepository<AssistantSession, UUID> { }
