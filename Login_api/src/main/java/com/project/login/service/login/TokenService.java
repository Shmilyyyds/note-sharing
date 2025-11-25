package com.project.login.service.login;

import com.project.login.model.entity.VerificationTokenEntity;
import com.project.login.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final VerificationTokenRepository tokenRepository;

    // 生成验证码并保存（若邮箱已有记录则更新）
    public String createToken(String email, String type) {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        Instant expiresAt = Instant.now().plusSeconds(300);

        // 查询是否已有该邮箱 + 类型的验证码
        Optional<VerificationTokenEntity> existingToken = tokenRepository
                .findByEmailAndTypeOrderByCreatedAtDesc(email, type);

        if (existingToken.isPresent()) {
            // 更新已有记录
            VerificationTokenEntity vt = existingToken.get();
            vt.setToken(code);
            vt.setExpiresAt(expiresAt);
            vt.setCreatedAt(Instant.now()); // 可更新创建时间或保留原时间
            tokenRepository.save(vt);
        } else {
            // 新建记录
            VerificationTokenEntity vt = new VerificationTokenEntity(email, code, type, expiresAt);
            tokenRepository.save(vt);
        }

        return code;
    }

    // 验证验证码
    public boolean validate(String email, String code, String type) {
        VerificationTokenEntity vt = tokenRepository.findByEmailAndType(email, type);

        if (vt == null) return false;

        String token = vt.getToken();
        return token.equals(code);
    }
}
