package com.nimbusventure.band.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticationController {

    private final AuthenticationService service;

    @GetMapping("/isUserExists")
    public ResponseEntity<Boolean> isUserExists(
            @Email @RequestParam(name="email") String email
    ) {
        return ResponseEntity.ok(service.isUserExists(email));
    }

    @GetMapping("/isBandExists")
    public ResponseEntity<Boolean> isBandExists(
            @NotBlank @RequestParam(name="bandId") String bandId
    ) {
        return ResponseEntity.ok(service.isBandExists(bandId));
    }

    @GetMapping("/validateJwt")
    public ResponseEntity<Boolean> validateJwt(
            @Valid @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.validateJwt(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/registerPet")
    public ResponseEntity<Boolean> registerPet(
            @Valid @RequestBody RegisterPetRequest request
    ) {
        return ResponseEntity.ok(service.registerPet(request));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Boolean> resetPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(service.resetPassword(request));
    }

    @GetMapping("/confirmRegistration")
    public ResponseEntity<Boolean> confirmRegistration(
            @Valid @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.confirmRegistration(token));
    }

    @GetMapping("/confirmResetPassword")
    public ResponseEntity<Boolean> confirmResetPassword(
            @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.confirmResetPassword(token));
    }
}
