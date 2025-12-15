package ar.uba.fi.ingsoft1.sistema_comedores.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ar.uba.fi.ingsoft1.sistema_comedores.user.email.EmailVerificationService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.InvalidCredentialsException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.login.UserLoginDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.RefreshTokenService;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.config.consts.MessageConfig;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserService userService;

    private static final String USERNAME = "user@fi.uba.ar";
    private static final String PASSWORD = "Password123";
    private static final String FIRST_NAME = "Juan";
    private static final String LAST_NAME = "Perez";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1995, 3, 1);
    private static final String ADDRESS = "Paseo Colón 850";
    private static final Gender GENDER = Gender.MALE;
    private static final UserRole ROLE = UserRole.STUDENT;

    @BeforeEach
    void setup() {
        var passwordEncoder = new BCryptPasswordEncoder();
        var passwordHash = passwordEncoder.encode(PASSWORD);

        UserRepository userRepository = mock();
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        
        // Create a verified and active user for successful login test
        User verifiedUser = new User(USERNAME, passwordHash, FIRST_NAME, LAST_NAME, ADDRESS, BIRTH_DATE, GENDER, ROLE);
        verifiedUser.setIsActive(true);
        
        when(userRepository.findByEmail(USERNAME))
                .thenReturn(Optional.of(verifiedUser));
        
        EmailVerificationService mockEmailVerificationService = mock(EmailVerificationService.class);
        RefreshTokenService mockRefreshTokenService = mock(RefreshTokenService.class);
        MessageConfig mockMessageConfig = mock(MessageConfig.class);
        
        // Mock messageConfig to return proper messages
        var authMessages = mock(MessageConfig.Auth.class);
        when(mockMessageConfig.getAuth()).thenReturn(authMessages);
        when(authMessages.getInvalidCredentials()).thenReturn("Invalid credentials");
        when(authMessages.getEmailNotVerified()).thenReturn("Email not verified");
        
        // Mock refreshTokenService to return a token pair
        var mockTokenDTO = mock(ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenDTO.class);
        when(mockRefreshTokenService.generateTokenPair(any(User.class))).thenReturn(mockTokenDTO);
        
        userService = new UserService(
                new BCryptPasswordEncoder(),
                userRepository,
                mockRefreshTokenService,
                mockEmailVerificationService,
                mockMessageConfig
        );
    }

    @Test
    void loginUser() {
        var response = userService.loginUser(new UserLoginDTO(USERNAME, PASSWORD));
        assertNotNull(response);
    }

    @Test
    void loginWithWrongPassword() {
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.loginUser(new UserLoginDTO(USERNAME, PASSWORD + "_wrong"));
        });
    }

    @Test
    void loginNonexistentUser() {
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.loginUser(new UserLoginDTO(USERNAME + "_wrong", PASSWORD));
        });
    }
}