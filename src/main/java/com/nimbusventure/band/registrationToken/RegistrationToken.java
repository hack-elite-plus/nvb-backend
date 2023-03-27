package com.nimbusventure.band.registrationToken;

import com.nimbusventure.band.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

@Data
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationToken {
    @Id
    @SequenceGenerator(
            name="token_sequence",
            sequenceName="token_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "token_sequence"
    )
    @Column(
            name="token_id",
            updatable = false
    )
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    @Enumerated(EnumType.STRING)
    private RegistrationTokenType tokenType;

    @ManyToOne
    @JoinColumn(
            nullable = false,
            name="user_id"
    )
    private User user;
}
