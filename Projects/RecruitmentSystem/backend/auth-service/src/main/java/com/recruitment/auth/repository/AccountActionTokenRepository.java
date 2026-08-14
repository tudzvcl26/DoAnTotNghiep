package com.recruitment.auth.repository;

import com.recruitment.auth.entity.AccountActionPurpose;
import com.recruitment.auth.entity.AccountActionToken;
import com.recruitment.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountActionTokenRepository extends JpaRepository<AccountActionToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountActionToken> findByTokenHashAndPurpose(String tokenHash, AccountActionPurpose purpose);

    List<AccountActionToken> findAllByUserAndPurposeAndUsedAtIsNullAndRevokedAtIsNull(
            User user, AccountActionPurpose purpose);

    Optional<AccountActionToken> findFirstByUserAndPurposeOrderByCreatedAtDesc(
            User user, AccountActionPurpose purpose);
}
