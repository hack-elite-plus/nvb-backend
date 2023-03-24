package com.nimbusventure.band.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    @Email(message = "Email is not valid!")
    private String email;

    @NotBlank(message="current password is required!")
    @ValidPassword(message = "password format is not valid!")
    private String currentPassword;

    @NotBlank(message="Password is required!")
    @ValidPassword(message = "password format is not valid!")
    private String newPassword;
}
