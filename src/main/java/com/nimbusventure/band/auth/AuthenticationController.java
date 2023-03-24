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

    @PostMapping("/checkEmail")
    public ResponseEntity<Boolean> checkEmail(
            @Email @RequestParam(name="email") String email
    ) {
        return ResponseEntity.ok(service.checkEmail(email));
    }

    @PostMapping("/checkBandId")
    public ResponseEntity<Boolean> checkBandId(
            @NotBlank @RequestParam(name="bandId") String bandId
    ) {
        return ResponseEntity.ok(service.checkBandId(bandId));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        System.out.println(request);
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<AuthenticationResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        return ResponseEntity.ok(service.forgotPassword(request));
    }

    @GetMapping("/confirmEmail")
    public ResponseEntity<AuthenticationResponse> confirmEmail(
            @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.confirmEmail(token));
    }

    @GetMapping("/confirmPasswordReset")
    public ResponseEntity<AuthenticationResponse> confirmPasswordReset(
            @RequestParam(name="token") String token
    ) {
        return ResponseEntity.ok(service.confirmPasswordReset(token));
    }
}
