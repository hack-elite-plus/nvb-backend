package com.nimbusventure.band.token;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    public void setVerifiedAt(Token token) {
        var confirmedToken = Token.builder()
                .verifiedAt(LocalDateTime.now())
                .build();
        tokenRepository.save(confirmedToken);
    }
}
