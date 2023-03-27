package com.nimbusventure.band.RegistrationToken;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationTokenService {
    private final RegistrationTokenRepository tokenRepository;

    public void setVerifiedAt(String token) {
        tokenRepository.setVerifiedAt(
                token, LocalDateTime.now());
    }
}
