package ar.uba.fi.ingsoft1.sistema_comedores.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import ar.uba.fi.ingsoft1.sistema_comedores.config.consts.MessageConfig;
import ar.uba.fi.ingsoft1.sistema_comedores.user.admin.CreateStaffDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.email.EmailVerificationService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.AccountNotVerifiedException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.InvalidCredentialsException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateRequestDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateResponseDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.RefreshDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.RefreshTokenService;

import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;    
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final MessageConfig messageConfig;
    
    UserService(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        RefreshTokenService refreshTokenService,
        @Lazy EmailVerificationService emailVerificationService,
        MessageConfig messageConfig
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationService = emailVerificationService;
        this.messageConfig = messageConfig;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(username)
                .orElseThrow(() -> {
                    var msg = String.format("Username '%s' not found", username);
                    return new UsernameNotFoundException(msg);
                });
    }

    public User getUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    var msg = String.format("Username '%s' not found", email);
                    return new UsernameNotFoundException(msg);
                });
    }

    public boolean userExists(String username) {
        return userRepository.findByEmail(username) != null;
    }

    public Optional<UserCreateResponseDTO> createUser(UserCreateRequestDTO data) {
        // Check if user already exists (by username or email)
        if (userRepository.findByEmail(data.getEmail()).isPresent()) {
            return Optional.empty(); // User already exists
        } else {
            var user = data.asUser(passwordEncoder::encode);
            userRepository.save(user);
            
            // Send verification email
            emailVerificationService.sendInitialVerificationEmail(user);
            
            return Optional.of(UserCreateResponseDTO.from(user)); // User created successfully
        }
    }

    public Optional<UserCreateResponseDTO> createStaffUser(CreateStaffDTO data) {
        // Check if user already exists (by username or email)
        if (userRepository.findByEmail(data.email()).isPresent()) {
            return Optional.empty();
        }
        User staffUser = data.asUser(passwordEncoder::encode);
        staffUser.setIsActive(true);
        staffUser.setMustChangePassword(true);
        userRepository.save(staffUser);
            
        return Optional.of(UserCreateResponseDTO.from(staffUser)); // User created successfully
    }


    public TokenDTO loginUser(UserCredentials data) {
        Optional<User> maybeUser = userRepository.findByEmail(data.getEmail());
        
        if (maybeUser.isEmpty()) {
            throw new InvalidCredentialsException(messageConfig.getAuth().getInvalidCredentials());
        }
        
        User user = maybeUser.get();
        
        // Check password
        if (!passwordEncoder.matches(data.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(messageConfig.getAuth().getInvalidCredentials());
        }
        
        // Check if email is verified
        if (!user.isActive()) {
            throw new AccountNotVerifiedException(messageConfig.getAuth().getEmailNotVerified());
        }
        
        return refreshTokenService.generateTokenPair(user);
    }

    public Optional<TokenDTO> refresh(RefreshDTO data) {
        return refreshTokenService.refresh(data.refreshToken());
    }

    public void logout(RefreshDTO data) {
        refreshTokenService.validateRefreshToken(data.refreshToken())
            .ifPresent(refreshTokenService::revokeRefreshToken);
    }
}
