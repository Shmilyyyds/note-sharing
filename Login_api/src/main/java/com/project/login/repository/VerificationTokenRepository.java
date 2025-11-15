package com.project.login.repository;

import com.project.login.model.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findTopByEmailAndTypeOrderByCreatedAtDesc(String email, String type);

}
