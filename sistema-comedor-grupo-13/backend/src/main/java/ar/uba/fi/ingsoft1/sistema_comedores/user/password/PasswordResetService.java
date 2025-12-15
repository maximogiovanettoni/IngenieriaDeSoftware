package ar.uba.fi.ingsoft1.sistema_comedores.user.password;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.config.email.EmailService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.*;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.Token;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenService;
import jakarta.transaction.Transactional;


@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    private final TokenService tokenService;

    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("The requested user for password reset does not exist"));

        Token passwordResetToken = tokenService.createFor(user, TokenType.PASSWORD_RESET);

        try{
            emailService.sendResetPasswordEmail(email, email, passwordResetToken.getValue());
        } catch (Exception e) {
            log.error("Error sending password reset email to " + user.getEmail() + ": " + e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Token passwordResetToken = tokenService.findByValueAndTokenType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new TokenNotFoundException("Token no existe o ya fue utilizado"));

        if (passwordResetToken.isExpired()) {
            throw new TokenExpiredException("El token ha expirado");
        }

        User user = passwordResetToken.getUser();
        
        user.setPassword(passwordEncoder.encode(newPassword));

        tokenService.invalidate(passwordResetToken);
        
        log.info("Password for user " + user.getEmail() + "was reset successfully!");
    }
}