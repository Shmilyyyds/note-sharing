package com.project.login.repository;

import com.project.login.model.entity.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, Long> {

    Optional<VerificationTokenEntity> findByEmailAndTypeOrderByCreatedAtDesc(String email, String type);

    VerificationTokenEntity findByEmailAndType(String email, String type);
}
