package com.nimbusventure.band.auth;

import com.nimbusventure.band.pet.Gender;
import com.nimbusventure.band.pet.PetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPetRequest {

    @NotBlank
    private String email;

    @Enumerated(EnumType.STRING)
    private PetType petType;

    private LocalDate dateOfBirth;

    @NotBlank
    private String name;

    @NotBlank
    private Gender gender;


}
