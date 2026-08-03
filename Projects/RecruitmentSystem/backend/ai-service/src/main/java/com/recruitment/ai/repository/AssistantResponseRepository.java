package com.recruitment.ai.repository;

import com.recruitment.ai.entity.AssistantResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssistantResponseRepository extends JpaRepository<AssistantResponse, UUID> { }
