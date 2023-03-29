package com.nimbusventure.band.auth;

import com.nimbusventure.band.user.Gender;
import com.nimbusventure.band.user.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Email
    private String email;

    @NotBlank
    private String password;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Past
    private LocalDate dateOfBirth;

    @NotNull
    private String bandId;
}
