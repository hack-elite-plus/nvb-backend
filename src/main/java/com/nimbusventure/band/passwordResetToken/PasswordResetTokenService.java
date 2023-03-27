package com.nimbusventure.band.passwordResetToken;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public void setVerifiedAt(String token) {
        passwordResetTokenRepository.setVerifiedAt(
                token, LocalDateTime.now());
    }
}
