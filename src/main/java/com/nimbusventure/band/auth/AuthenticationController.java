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

    @PostMapping("/isUserExists")
    public ResponseEntity<Boolean> isUserExists(
            @Email @RequestParam(name="email") String email
    ) {
        return ResponseEntity.ok(service.isUserExists(email));
    }

    @PostMapping("/isBandExists")
    public ResponseEntity<Boolean> isBandExists(
            @NotBlank @RequestParam(name="bandId") String bandId
    ) {
        return ResponseEntity.ok(service.isBandExists(bandId));
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
        System.out.println(request);
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/confirmRegistration")
    public ResponseEntity<Boolean> confirmRegistration(
            @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.confirmRegistration(token));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Boolean> resetPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(service.resetPassword(request));
    }

    @GetMapping("/confirmResetPassword")
    public ResponseEntity<Boolean> confirmResetPassword(
            @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.confirmResetPassword(token));
    }
}
