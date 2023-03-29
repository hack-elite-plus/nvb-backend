package com.nimbusventure.band.auth;

import com.nimbusventure.band.band.Band;
import com.nimbusventure.band.band.BandRepository;
import com.nimbusventure.band.config.JwtService;
import com.nimbusventure.band.passwordResetToken.PasswordResetToken;
import com.nimbusventure.band.passwordResetToken.PasswordResetTokenRepository;
import com.nimbusventure.band.passwordResetToken.PasswordResetTokenService;
import com.nimbusventure.band.pet.Pet;
import com.nimbusventure.band.pet.PetRepository;
import com.nimbusventure.band.registrationToken.RegistrationToken;
import com.nimbusventure.band.registrationToken.RegistrationTokenRepository;
import com.nimbusventure.band.email.EmailSender;
import com.nimbusventure.band.registrationToken.RegistrationTokenService;
import com.nimbusventure.band.registrationToken.RegistrationTokenType;
import com.nimbusventure.band.user.Role;
import com.nimbusventure.band.user.User;
import com.nimbusventure.band.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserDetailsService userDetailsService;

    private final RegistrationTokenService registrationTokenService;
    private final RegistrationTokenRepository registrationTokenRepository;

    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final UserRepository userRepository;
    private final BandRepository bandRepository;

    private final PetRepository petRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSender emailSender;

    @Transactional
    public Boolean register(RegisterRequest request) {
        var isUserExists = userRepository.findUserByEmail(request.getEmail());
        var isBandExists = bandRepository.findBandByBandId(request.getBandId());

        if (isUserExists.isPresent()) throw new IllegalStateException("Username is already exists!");
        if (isBandExists.isPresent())
            throw new IllegalStateException("Band ID is already registered to a different user.");

        var user = User.builder()
                .firstName(request.getFirstName().trim().toLowerCase())
                .lastName(request.getLastName().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .dateOfBirth(request.getDateOfBirth())
                .userType(request.getUserType())
                .gender(request.getGender())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        var band = new Band(request.getBandId(), user);

        userRepository.save(user);
        bandRepository.save(band);

        var jwtToken = jwtService.generateToken(user);

        var token = RegistrationToken.builder()
                .token(jwtToken)
                .createdAt(LocalDateTime.now())
                .tokenType(RegistrationTokenType.EMAIL_CONFIRM)
                .user(user)
                .build();

        registrationTokenRepository.save(token);

        String link = "http://localhost:8080/api/v1/auth/confirmRegistration?token=" + jwtToken;
        emailSender.send(request.getEmail(), buildEmailConfirmationTemplate(request.getFirstName(), link), "Confirm your email");

        return true;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findUserByEmail(request.getEmail());
        if(user.isEmpty()) throw new IllegalStateException("username is not found!");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().trim().toLowerCase(),
                            request.getPassword()
                    )
            );
        } catch(Exception e) {
            throw new IllegalStateException("password is invalid!");
        }

        if(!user.get().isVerified()) throw new IllegalStateException("user is not verified! please verify your account!");

        var jwtToken = jwtService.generateToken(user.get());

        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }

    public Boolean resetPassword(ForgotPasswordRequest request) {
        var user = userRepository.findUserByEmail(request.getEmail());

        if (user.isEmpty()) throw new IllegalStateException("User not found!");

        var jwtToken = jwtService.generateToken(user.get());

        var passwordResetToken = PasswordResetToken.builder()
                .token(jwtToken)
                .newPassword(passwordEncoder.encode(request.getNewPassword()))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user.get())
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String link = "http://localhost:8080/api/v1/auth/confirmResetPassword?token=" + jwtToken;
        emailSender.send(request.getEmail(), buildPasswordResetConfirmationTemplate(link), "Reset your password");
        return true;
    }

    public Boolean validateJwt(String token) {
        var username = jwtService.extractUsername(token);
        if(username == null) throw new IllegalStateException("username not found!");

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtService.isTokenValid(token, userDetails);
    }

    public Boolean registerPet(RegisterPetRequest request) {
        var user = userRepository.findUserByEmail(request.getEmail());

        if(user.isEmpty()) throw new IllegalStateException("user not found!");

        var pet = Pet.builder()
                .type(request.getPetType())
                .user(user.get())
                .gender(request.getGender())
                .name(request.getName())
                .build();

        petRepository.save(pet);
        return true;
    }

    @Transactional
    public Boolean confirmRegistration(String token) {
        var confirmationToken = registrationTokenRepository.findByToken(token);
        if (confirmationToken.isEmpty() || confirmationToken.get().getTokenType() != RegistrationTokenType.EMAIL_CONFIRM)
            throw new IllegalStateException("invalid token!");

        if(confirmationToken.get().getVerifiedAt() != null)
            throw new IllegalStateException("email is already confirmed!");

        registrationTokenService.setVerifiedAt(confirmationToken.get().getToken());

        var userId = confirmationToken.get().getUser().getId();
        userRepository.setUserVerified(userId);

        return true;
    }

    @Transactional
    public Boolean confirmResetPassword(String token) {
        var passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken.isEmpty()) throw new IllegalStateException("invalid token!");

        if(passwordResetToken.get().getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalStateException("token expired!");

        if(passwordResetToken.get().getVerifiedAt() != null)
            throw new IllegalStateException("email is already confirmed!");

        passwordResetTokenService.setVerifiedAt(passwordResetToken.get().getToken());

        var newPassword = passwordResetToken.get().getNewPassword();
        var userId = passwordResetToken.get().getUser().getId();
        userRepository.updateUserPassword(userId, newPassword);
        return true;
    }

    public Boolean isUserExists(String email) {
        if(email.length() == 0) throw new IllegalStateException("email is not valid");

        var user = userRepository.findUserByEmail(email);
        if (user.isPresent()) throw new IllegalStateException("Username is already exists!");
        else return false;
    }

    public Boolean isBandExists(String bandId) {
        if(bandId.length() == 0) throw new IllegalStateException("bandId is not valid");

        var band = bandRepository.findBandByBandId(bandId);
        if(band.isPresent()) throw new IllegalStateException("Band ID is already registered to a different user.");
        else return false;
    }

    private String buildEmailConfirmationTemplate(String firstName, String link) {
        return "                    <table\n"+
               "                      style=\"\n"+
               "                        background-color: #ffffff;\n"+
               "                        padding-top: 20px;\n"+
               "                        color: #434245;\n"+
               "                        width: 100%;\n"+
               "                        border: 0;\n"+
               "                        text-align: center;\n"+
               "                        border-collapse: collapse;\n"+
               "                      \"\n"+
               "                    >\n"+
               "                      <tbody>\n"+
               "                        <tr>\n"+
               "                          <td style=\"vertical-align: top; padding: 0\">\n"+
               "                            <center>\n"+
               "                              <table\n"+
               "                                style=\"\n"+
               "                                  border: 0;\n"+
               "                                  border-collapse: collapse;\n"+
               "                                  margin: 0 auto;\n"+
               "                                  background: white;\n"+
               "                                  border-radius: 8px;\n"+
               "                                  margin-bottom: 16px;\n"+
               "                                  font-family: system-ui, -apple-system, BlinkMacSystemFont,\n"+
               "                                    \"Segoe UI\", Roboto, Oxygen, Ubuntu, Cantarell, \"Open Sans\",\n"+
                "                                    \"Helvetica Neue\", sans-serif;\n"+
                "                                \"\n"+
                "                              >\n"+
                "                                <tbody>\n"+
                "                                  <tr>\n"+
                "                                    <td\n"+
                "                                      style=\"\n"+
                "                                        width: 546px;\n"+
                "                                        vertical-align: top;\n"+
                "                                        padding-top: 32px;\n"+
                "                                      \"\n"+
                "                                    >\n"+
                "                                      <div style=\"max-width: 600px; margin: 0 auto\">\n"+
                "                                        <div\n"+
                "                                          style=\"\n"+
                "                                            margin-left: 50px;\n"+
                "                                            margin-right: 50px;\n"+
                "                                            margin-bottom: 72px;\n"+
                "                                            margin-bottom: 30px;\n"+
                "                                          \"\n"+
                "                                        >\n"+
                "                                          <div style=\"margin-top: 18px\">\n"+
                "                                            <img\n"+
                "                                              width=\"100\"\n"+
                "                                              height=\"36\"\n"+
                "                                              style=\"\n"+
                "                                                margin-top: 0;\n"+
                "                                                margin-right: 0;\n"+
                "                                                margin-bottom: 32px;\n"+
                "                                                margin-left: 0px;\n"+
                "                                              \"\n"+
                "                                              src=\"https://nimbusventure.com/wp-content/uploads/2021/11/cropped-logo.png\"\n"+
                "                                              alt=\"nimbus venture logo\"\n"+
                "                                              data-bit=\"iit\"\n"+
                "                                            />\n"+
                "                                          </div>\n"+
                "                                          <h1>Hi " + firstName + ",</h1>\n"+
                "                                          <h1>Confirm your email address</h1>\n"+
                "                                          <p\n"+
                "                                            style=\"\n"+
                "                                              font-size: 20px;\n"+
                "                                              line-height: 28px;\n"+
                "                                              letter-spacing: -0.2px;\n"+
                "                                              margin-bottom: 28px;\n"+
                "                                              word-break: break-word;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            Your confirmation link is below — Please click on\n"+
                "                                            the link below to confirm your email address.\n"+
                "                                          </p>\n"+
                "                                        </div>\n"+
                "                                        <center>\n"+
                "                                          <a\n"+
                "                                            href=" + link + "\n"+
                "                                            style=\"\n"+
                "                                              text-decoration: none;\n"+
                "                                              padding: 8px 15px;\n"+
                "                                              background-color: #092de0;\n"+
                "                                              border-radius: 50px;\n"+
                "                                              color: #fff;\n"+
                "                                              text-align: center;\n"+
                "                                              font-weight: 600;\n"+
                "                                \n"+
                "                                              font-size: 18px;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            Confirm my account\n"+
                "                                          </a>\n"+
                "                                        </center>\n"+
                "                                        <div\n"+
                "                                          style=\"\n"+
                "                                            margin-left: 50px;\n"+
                "                                            margin-right: 50px;\n"+
                "                                            margin-bottom: 72px;\n"+
                "                                            margin-bottom: 30px;\n"+
                "                                          \"\n"+
                "                                        >\n"+
                "                                          <p\n"+
                "                                            style=\"\n"+
                "                                              font-size: 16px;\n"+
                "                                              line-height: 24px;\n"+
                "                                              letter-spacing: -0.2px;\n"+
                "                                              margin-bottom: 28px;\n"+
                "                                            \"\n"+
                "                                          ></p>\n"+
                "                                          <p\n"+
                "                                            style=\"\n"+
                "                                              font-size: 16px;\n"+
                "                                              line-height: 24px;\n"+
                "                                              letter-spacing: -0.2px;\n"+
                "                                              margin-bottom: 28px;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            If you didn’t request this email, there’s nothing to\n"+
                "                                            worry about — you can safely ignore it.\n"+
                "                                          </p>\n"+
                "                                        </div>\n"+
                "                                      </div>\n"+
                "                                    </td>\n"+
                "                                  </tr>\n"+
                "                                </tbody>\n"+
                "                              </table>\n"+
                "                            </center>\n"+
                "                          </td>\n"+
                "                        </tr>\n"+
                "                        <tr>\n"+
                "                          <td\n"+
                "                            style=\"\n"+
                "                              font-size: 15px;\n"+
                "                              color: #717274;\n"+
                "                              text-align: center;\n"+
                "                              width: 100%;\n"+
                "                            \"\n"+
                "                          >\n"+
                "                            <center>\n"+
                "                              <table\n"+
                "                                style=\"\n"+
                "                                  margin: 20px auto 0;\n"+
                "                                  background-color: white;\n"+
                "                                  border: 0;\n"+
                "                                  text-align: center;\n"+
                "                                  border-collapse: collapse;\n"+
                "                                \"\n"+
                "                              >\n"+
                "                                <tbody>\n"+
                "                                  <tr>\n"+
                "                                    <td style=\"width: 546px; vertical-align: top; padding: 0px\">\n"+
                "                                      <div style=\"max-width: 600px; margin: 0 auto\">\n"+
                "                                        <div style=\"padding: 0 50px\">\n"+
                "                                          <table>\n"+
                "                                            <tbody>\n"+
                "                                              <tr>\n"+
                "                                                <td\n"+
                "                                                  style=\"vertical-align: top; text-align: left\"\n"+
                "                                                >\n"+
                "                                                  <img\n"+
                "                                                    width=\"100\"\n"+
                "                                                    height=\"36\"\n"+
                "                                                    style=\"\n"+
                "                                                      margin-top: 0;\n"+
                "                                                      margin-right: 0;\n"+
                "                                                      margin-bottom: 32px;\n"+
                "                                                      margin-left: 0px;\n"+
                "                                                    \"\n"+
                "                                                    src=\"https://nimbusventure.com/wp-content/uploads/2021/11/cropped-logo.png\"\n"+
                "                                                    alt=\"nimbus venture logo\"\n"+
                "                                                    data-bit=\"iit\"\n"+
                "                                                  />\n"+
                "                                                </td>\n"+
                "                                                <td\n"+
                "                                                  style=\"vertical-align: top; text-align: right\"\n"+
                "                                                >\n"+
                "                                                  <a\n"+
                "                                                    href=\"https://www.facebook.com/nimbusventurepage\"\n"+
                "                                                    style=\"margin-left: 32px\"\n"+
                "                                                    target=\"_blank\"\n"+
                "                                                    data-saferedirecturl=\"https://www.google.com/url?q=https://www.facebook.com/nimbusventurepage\"\n"+
                "                                                    ><img\n"+
                "                                                      src=\"https://ci5.googleusercontent.com/proxy/4MPp8ZT9T9bGo_-GbffzPt76cT7mYJAOAzOvZEI1Oyb8wRtf9tR8EuNPNj3DTV82bvpse64nNQhX4gDaOh3ox7XK2ZkTszFHZBu4vFvdF-_gIv9-8BYwGIu7LnUFvU9Cdg=s0-d-e1-ft#https://a.slack-edge.com/b8be608/marketing/img/icons/icon_colored_facebook.png\"\n"+
                "                                                      width=\"32\"\n"+
                "                                                      height=\"32\"\n"+
                "                                                      title=\"Facebook\"\n"+
                "                                                      data-bit=\"iit\" /></a\n"+
                "                                                  ><a\n"+
                "                                                    href=\"https://www.linkedin.com/company/nimbus-venture-pvt-ltd-/\"\n"+
                "                                                    style=\"margin-left: 32px\"\n"+
                "                                                    target=\"_blank\"\n"+
                "                                                    data-saferedirecturl=\"https://www.google.com/url?q=https://www.linkedin.com/company/nimbus-venture-pvt-ltd-\"\n"+
                "                                                    ><img\n"+
                "                                                      src=\"https://ci3.googleusercontent.com/proxy/uyzUcsRXlGvU0avQztKemD0Pas-VJx26-bl6uNMbAJr3jOZAK2KFvdX6xoKA5l-gRqR_H7Yhrc-Jvfy6niN3zkx4oP1pnDdrgE0UCODfKXAlL2jSoKg3mzv1GH-a96uP2A=s0-d-e1-ft#https://a.slack-edge.com/b8be608/marketing/img/icons/icon_colored_linkedin.png\"\n"+
                "                                                      width=\"32\"\n"+
                "                                                      height=\"32\"\n"+
                "                                                      title=\"LinkedIn\"\n"+
                "                                                      data-bit=\"iit\"\n"+
                "                                                  /></a>\n"+
                "                                                </td>\n"+
                "                                              </tr>\n"+
                "                                            </tbody>\n"+
                "                                          </table>\n"+
                "                                          <div\n"+
                "                                            style=\"\n"+
                "                                              font-size: 12px;\n"+
                "                                              opacity: 0.5;\n"+
                "                                              color: #696969;\n"+
                "                                              text-align: left;\n"+
                "                                              line-height: 15px;\n"+
                "                                              margin-bottom: 50px;\n"+
                "                                              text-align: left;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            <a\n"+
                "                                              href=\"https://nimbusventure.com/\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/\"\n"+
                "                                              >Home</a\n"+
                "                                            >&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a\n"+
                "                                              href=\"https://nimbusventure.com/index.php/products-we-offer/\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/index.php/products-we-offer/\"\n"+
                "                                              >Products</a\n"+
                "                                            >&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a\n"+
                "                                              href=\"https://nimbusventure.com/#about\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/#about\"\n"+
                "                                              >About Us</a\n"+
                "                                            >&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a\n"+
                "                                              href=\"https://nimbusventure.com/#contact\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/#contact\"\n"+
                "                                              >Contact Us</a\n"+
                "                                            ><br /><br />\n"+
                "                                            <div>\n"+
                "                                              ©2023 Nimbus Venture, a Software company.<br />\n"+
                "                                              No 07, 8th Lane, Nawala Rd, Sri Jayawardenepura\n"+
                "                                              Kotte, 10100 Sri Lanka\n"+
                "                                            </div>\n"+
                "                                            <br />All rights reserved.\n"+
                "                                          </div>\n"+
                "                                        </div>\n"+
                "                                      </div>\n"+
                "                                    </td>\n"+
                "                                  </tr>\n"+
                "                                </tbody>\n"+
                "                              </table>\n"+
                "                            </center>\n"+
                "                          </td>\n"+
                "                        </tr>\n"+
                "                      </tbody>\n"+
                "                    </table>";
    }

    private String buildPasswordResetConfirmationTemplate(String link) {
        return "                    <table\n"+
                "                      style=\"\n"+
                "                        background-color: #ffffff;\n"+
                "                        padding-top: 20px;\n"+
                "                        color: #434245;\n"+
                "                        width: 100%;\n"+
                "                        border: 0;\n"+
                "                        text-align: center;\n"+
                "                        border-collapse: collapse;\n"+
                "                      \"\n"+
                "                    >\n"+
                "                      <tbody>\n"+
                "                        <tr>\n"+
                "                          <td style=\"vertical-align: top; padding: 0\">\n"+
                "                            <center>\n"+
                "                              <table\n"+
                "                                style=\"\n"+
                "                                  border: 0;\n"+
                "                                  border-collapse: collapse;\n"+
                "                                  margin: 0 auto;\n"+
                "                                  background: white;\n"+
                "                                  border-radius: 8px;\n"+
                "                                  margin-bottom: 16px;\n"+
                "                                  font-family: system-ui, -apple-system, BlinkMacSystemFont,\n"+
                "                                    \"Segoe UI\", Roboto, Oxygen, Ubuntu, Cantarell, \"Open Sans\",\n"+
                "                                    \"Helvetica Neue\", sans-serif;\n"+
                "                                \"\n"+
                "                              >\n"+
                "                                <tbody>\n"+
                "                                  <tr>\n"+
                "                                    <td\n"+
                "                                      style=\"\n"+
                "                                        width: 546px;\n"+
                "                                        vertical-align: top;\n"+
                "                                        padding-top: 32px;\n"+
                "                                      \"\n"+
                "                                    >\n"+
                "                                      <div style=\"max-width: 600px; margin: 0 auto\">\n"+
                "                                        <div\n"+
                "                                          style=\"\n"+
                "                                            margin-left: 50px;\n"+
                "                                            margin-right: 50px;\n"+
                "                                            margin-bottom: 72px;\n"+
                "                                            margin-bottom: 30px;\n"+
                "                                          \"\n"+
                "                                        >\n"+
                "                                          <div style=\"margin-top: 18px\">\n"+
                "                                            <img\n"+
                "                                              width=\"100\"\n"+
                "                                              height=\"36\"\n"+
                "                                              style=\"\n"+
                "                                                margin-top: 0;\n"+
                "                                                margin-right: 0;\n"+
                "                                                margin-bottom: 32px;\n"+
                "                                                margin-left: 0px;\n"+
                "                                              \"\n"+
                "                                              src=\"https://nimbusventure.com/wp-content/uploads/2021/11/cropped-logo.png\"\n"+
                "                                              alt=\"nimbus venture logo\"\n"+
                "                                              data-bit=\"iit\"\n"+
                "                                            />\n"+
                "                                          </div>\n"+
                "                                          <h1>Reset your password</h1>\n"+
                "                                          <p\n"+
                "                                            style=\"\n"+
                "                                              font-size: 20px;\n"+
                "                                              line-height: 28px;\n"+
                "                                              letter-spacing: -0.2px;\n"+
                "                                              margin-bottom: 28px;\n"+
                "                                              word-break: break-word;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                           <p>You told us you forgot your password.\n"+
                "                                              If you really did, click here to choose a new one:\n"+
                "                                          </p>\n"+
                "                                        </div>\n"+
                "                                        <center>\n"+
                "                                          <a\n"+
                "                                            href=" + link + "\n"+
                "                                            style=\"\n"+
                "                                              text-decoration: none;\n"+
                "                                              padding: 8px 15px;\n"+
                "                                              background-color: #092de0;\n"+
                "                                              border-radius: 50px;\n"+
                "                                              color: #fff;\n"+
                "                                              text-align: center;\n"+
                "                                              font-weight: 600;\n"+
                "                                \n"+
                "                                              font-size: 18px;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            choose a new password\n"+
                "                                          </a>\n"+
                "                                        </center>\n"+
                "                                        <div\n"+
                "                                          style=\"\n"+
                "                                            margin-left: 50px;\n"+
                "                                            margin-right: 50px;\n"+
                "                                            margin-bottom: 72px;\n"+
                "                                            margin-bottom: 30px;\n"+
                "                                          \"\n"+
                "                                        >\n"+
                "                                          <p\n"+
                "                                            style=\"\n"+
                "                                              font-size: 16px;\n"+
                "                                              line-height: 24px;\n"+
                "                                              letter-spacing: -0.2px;\n"+
                "                                              margin-bottom: 28px;\n"+
                "                                            \"\n"+
                "                                          ></p>\n"+
                "                                          <p\n"+
                "                                            style=\"\n"+
                "                                              font-size: 16px;\n"+
                "                                              line-height: 24px;\n"+
                "                                              letter-spacing: -0.2px;\n"+
                "                                              margin-bottom: 28px;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            If you didn’t mean to reset your password, there’s nothing to\n"+
                "                                            worry about —  your password will not change.\n"+
                "                                          </p>\n"+
                "                                        </div>\n"+
                "                                      </div>\n"+
                "                                    </td>\n"+
                "                                  </tr>\n"+
                "                                </tbody>\n"+
                "                              </table>\n"+
                "                            </center>\n"+
                "                          </td>\n"+
                "                        </tr>\n"+
                "                        <tr>\n"+
                "                          <td\n"+
                "                            style=\"\n"+
                "                              font-size: 15px;\n"+
                "                              color: #717274;\n"+
                "                              text-align: center;\n"+
                "                              width: 100%;\n"+
                "                            \"\n"+
                "                          >\n"+
                "                            <center>\n"+
                "                              <table\n"+
                "                                style=\"\n"+
                "                                  margin: 20px auto 0;\n"+
                "                                  background-color: white;\n"+
                "                                  border: 0;\n"+
                "                                  text-align: center;\n"+
                "                                  border-collapse: collapse;\n"+
                "                                \"\n"+
                "                              >\n"+
                "                                <tbody>\n"+
                "                                  <tr>\n"+
                "                                    <td style=\"width: 546px; vertical-align: top; padding: 0px\">\n"+
                "                                      <div style=\"max-width: 600px; margin: 0 auto\">\n"+
                "                                        <div style=\"padding: 0 50px\">\n"+
                "                                          <table>\n"+
                "                                            <tbody>\n"+
                "                                              <tr>\n"+
                "                                                <td\n"+
                "                                                  style=\"vertical-align: top; text-align: left\"\n"+
                "                                                >\n"+
                "                                                  <img\n"+
                "                                                    width=\"100\"\n"+
                "                                                    height=\"36\"\n"+
                "                                                    style=\"\n"+
                "                                                      margin-top: 0;\n"+
                "                                                      margin-right: 0;\n"+
                "                                                      margin-bottom: 32px;\n"+
                "                                                      margin-left: 0px;\n"+
                "                                                    \"\n"+
                "                                                    src=\"https://nimbusventure.com/wp-content/uploads/2021/11/cropped-logo.png\"\n"+
                "                                                    alt=\"nimbus venture logo\"\n"+
                "                                                    data-bit=\"iit\"\n"+
                "                                                  />\n"+
                "                                                </td>\n"+
                "                                                <td\n"+
                "                                                  style=\"vertical-align: top; text-align: right\"\n"+
                "                                                >\n"+
                "                                                  <a\n"+
                "                                                    href=\"https://www.facebook.com/nimbusventurepage\"\n"+
                "                                                    style=\"margin-left: 32px\"\n"+
                "                                                    target=\"_blank\"\n"+
                "                                                    data-saferedirecturl=\"https://www.google.com/url?q=https://www.facebook.com/nimbusventurepage\"\n"+
                "                                                    ><img\n"+
                "                                                      src=\"https://ci5.googleusercontent.com/proxy/4MPp8ZT9T9bGo_-GbffzPt76cT7mYJAOAzOvZEI1Oyb8wRtf9tR8EuNPNj3DTV82bvpse64nNQhX4gDaOh3ox7XK2ZkTszFHZBu4vFvdF-_gIv9-8BYwGIu7LnUFvU9Cdg=s0-d-e1-ft#https://a.slack-edge.com/b8be608/marketing/img/icons/icon_colored_facebook.png\"\n"+
                "                                                      width=\"32\"\n"+
                "                                                      height=\"32\"\n"+
                "                                                      title=\"Facebook\"\n"+
                "                                                      data-bit=\"iit\" /></a\n"+
                "                                                  ><a\n"+
                "                                                    href=\"https://www.linkedin.com/company/nimbus-venture-pvt-ltd-/\"\n"+
                "                                                    style=\"margin-left: 32px\"\n"+
                "                                                    target=\"_blank\"\n"+
                "                                                    data-saferedirecturl=\"https://www.google.com/url?q=https://www.linkedin.com/company/nimbus-venture-pvt-ltd-\"\n"+
                "                                                    ><img\n"+
                "                                                      src=\"https://ci3.googleusercontent.com/proxy/uyzUcsRXlGvU0avQztKemD0Pas-VJx26-bl6uNMbAJr3jOZAK2KFvdX6xoKA5l-gRqR_H7Yhrc-Jvfy6niN3zkx4oP1pnDdrgE0UCODfKXAlL2jSoKg3mzv1GH-a96uP2A=s0-d-e1-ft#https://a.slack-edge.com/b8be608/marketing/img/icons/icon_colored_linkedin.png\"\n"+
                "                                                      width=\"32\"\n"+
                "                                                      height=\"32\"\n"+
                "                                                      title=\"LinkedIn\"\n"+
                "                                                      data-bit=\"iit\"\n"+
                "                                                  /></a>\n"+
                "                                                </td>\n"+
                "                                              </tr>\n"+
                "                                            </tbody>\n"+
                "                                          </table>\n"+
                "                                          <div\n"+
                "                                            style=\"\n"+
                "                                              font-size: 12px;\n"+
                "                                              opacity: 0.5;\n"+
                "                                              color: #696969;\n"+
                "                                              text-align: left;\n"+
                "                                              line-height: 15px;\n"+
                "                                              margin-bottom: 50px;\n"+
                "                                              text-align: left;\n"+
                "                                            \"\n"+
                "                                          >\n"+
                "                                            <a\n"+
                "                                              href=\"https://nimbusventure.com/\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/\"\n"+
                "                                              >Home</a\n"+
                "                                            >&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a\n"+
                "                                              href=\"https://nimbusventure.com/index.php/products-we-offer/\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/index.php/products-we-offer/\"\n"+
                "                                              >Products</a\n"+
                "                                            >&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a\n"+
                "                                              href=\"https://nimbusventure.com/#about\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/#about\"\n"+
                "                                              >About Us</a\n"+
                "                                            >&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<a\n"+
                "                                              href=\"https://nimbusventure.com/#contact\"\n"+
                "                                              style=\"color: #696969 !important\"\n"+
                "                                              target=\"_blank\"\n"+
                "                                              data-saferedirecturl=\"https://www.google.com/url?q=https://nimbusventure.com/#contact\"\n"+
                "                                              >Contact Us</a\n"+
                "                                            ><br /><br />\n"+
                "                                            <div>\n"+
                "                                              ©2023 Nimbus Venture, a Software company.<br />\n"+
                "                                              No 07, 8th Lane, Nawala Rd, Sri Jayawardenepura\n"+
                "                                              Kotte, 10100 Sri Lanka\n"+
                "                                            </div>\n"+
                "                                            <br />All rights reserved.\n"+
                "                                          </div>\n"+
                "                                        </div>\n"+
                "                                      </div>\n"+
                "                                    </td>\n"+
                "                                  </tr>\n"+
                "                                </tbody>\n"+
                "                              </table>\n"+
                "                            </center>\n"+
                "                          </td>\n"+
                "                        </tr>\n"+
                "                      </tbody>\n"+
                "                    </table>";
    }
}
