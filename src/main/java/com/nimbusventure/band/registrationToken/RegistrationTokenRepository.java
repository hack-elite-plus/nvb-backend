package com.nimbusventure.band.registrationToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, Long> {
    Optional<RegistrationToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE RegistrationToken rt " +
            "SET rt.verifiedAt = ?2 " +
            "WHERE rt.token = ?1")
    void setVerifiedAt(String token, LocalDateTime confirmedAt);
}
