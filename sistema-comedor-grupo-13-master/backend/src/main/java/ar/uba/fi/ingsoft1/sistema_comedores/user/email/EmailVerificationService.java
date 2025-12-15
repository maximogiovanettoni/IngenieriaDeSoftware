package ar.uba.fi.ingsoft1.sistema_comedores.user.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.config.consts.MessageConfig;
import ar.uba.fi.ingsoft1.sistema_comedores.config.email.EmailService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.*;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.Token;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenService;

@Service
public class EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final MessageConfig messageConfig;
    
    public EmailVerificationService(
            UserRepository userRepository,
            EmailService emailService,
            TokenService tokenService,
            MessageConfig messageConfig) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.messageConfig = messageConfig;
    }
    
    /**
     * Verifies a user's email using the verification token
     * @param token The verification token from the email link
     * @throws InvalidTokenException if token doesn't exist
     * @throws TokenExpiredException if token has expired
     * @throws EmailAlreadyVerifiedException if email is already verified
     */
    @Transactional
    public void verifyEmail(String token) {
        // Find user by verification token
        Token userToken = tokenService.findByValueAndTokenType(token, TokenType.EMAIL_VERIFICATION)
            .orElseThrow(() -> new TokenNotFoundException(messageConfig.getToken().getEmailVerification().getNotFound()));
        User user = userToken.getUser();
        
        // Check if email is already verified
        if (user.isActive()) {
            throw new EmailAlreadyVerifiedException(messageConfig.getAuth().getEmailAlreadyVerified());
        }
        
        // Check if token has expired
        if (userToken.isExpired()) {
            throw new TokenExpiredException(messageConfig.getToken().getEmailVerification().getExpired());
        }
        
        // Activate the user account
        user.setIsActive(true);
        
        // Clear the verification token (one-time use)
        tokenService.invalidate(userToken);
        
        // Log successful verification
        log.info("=== EMAIL VERIFIED ===");
        log.info("User: " + user.getEmail());
        log.info("Account activated: " + user.isActive());
        log.info("======================");
    }
    
    /**
     * Resends verification email for users who lost/didn't receive the original
     * @param email The user's email address
     * @throws UserNotFoundException if user doesn't exist
     * @throws EmailAlreadyVerifiedException if email is already verified
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(messageConfig.getAuth().getUserNotFound()));

        if (user.isActive()) {
            throw new EmailAlreadyVerifiedException(messageConfig.getAuth().getEmailAlreadyVerified());
        }

        createAndSendVerificationToken(user, true);
    }
    
    /**
     * Sends initial verification email when user registers
     * @param user The newly registered user
     */
    @Transactional
    public void sendInitialVerificationEmail(User user) {
        createAndSendVerificationToken(user, false);
    }

    /**
     * Helper: create token (via tokenService) and send the appropriate email.
     * Keeps token creation and sending logic in one place.
     */
    private void createAndSendVerificationToken(User user, boolean isResend) {
        // Use tokenService to create token and persist it consistently
        Token token = tokenService.createFor(user, TokenType.EMAIL_VERIFICATION);

        // tokenService should handle expiry/persistence — if not, handle it there
        try {
            if (isResend) {
                emailService.sendResendVerificationEmail(user.getEmail(), user.getEmail(), token.getValue());
                log.info("Resend verification email sent to {}", user.getEmail());
            } else {
                emailService.sendVerificationEmail(user.getEmail(), user.getEmail(), token.getValue());
                log.info("Initial verification email sent to {}", user.getEmail());
            }
        } catch (Exception e) {
            // Log the failure, but don't necessarily fail the whole transaction
            log.warn("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);
            // Optionally: schedule retry or publish an event to send email asynchronously
        }
    }

    /**
     * Check verification status of a user
     * @param email The user's email address
     * @return true if email is verified, false otherwise
     */
    public boolean isEmailVerified(String email) {
        return userRepository.findByEmail(email)
            .map(User::isActive)
            .orElse(false);
    }
}