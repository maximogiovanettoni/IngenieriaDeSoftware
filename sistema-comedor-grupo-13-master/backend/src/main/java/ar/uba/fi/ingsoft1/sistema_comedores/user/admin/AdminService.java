package ar.uba.fi.ingsoft1.sistema_comedores.user.admin;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.config.email.EmailService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.email.EmailVerificationService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateResponseDTO;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);


    private final UserService userService;
    private final UserRepository userRepository;
    private final StaffDeletionAuditRepository auditRepository;
    private final TokenService refreshTokenService;
    private final EmailService emailService;
    @Autowired
    public AdminService(UserService userService,
                        UserRepository userRepository,
                        StaffDeletionAuditRepository auditRepository,
                        TokenService refreshTokenService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    @Transactional
    public UserCreateResponseDTO createStaffUser(CreateStaffDTO req, String createdBy) {
        UserCreateResponseDTO staffUserCreated = userService.createStaffUser(req)
                .orElseThrow(() -> new UserAlreadyExistsException("El email ya está registrado para un usuario."));
        try {
            emailService.sendStaffCreationEmail(req.getEmail(), req.getPassword());
        } catch (Exception ex) {
            log.warn("No se pudo enviar el email al nuevo usuario staff: " + ex.getMessage());
        }
        return staffUserCreated;
    }

    @Transactional
    public void deleteStaff(String staffEmail, String deletedBy, String reason) {

        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new UserNotFoundException(staffEmail));

        if (!UserRole.STAFF.equals(staff.getRole())) {
            throw new IllegalArgumentException("Solo se pueden eliminar usuarios con role STAFF");
        }

        if (staff.getEmail().equalsIgnoreCase(deletedBy)) {
            throw new IllegalStateException("No puedes eliminar tu propia cuenta");
        }

        refreshTokenService.invalidateAllForUser(staff, TokenType.REFRESH);

        StaffDeletionAudit audit = new StaffDeletionAudit(
                staff.getEmail(),
                deletedBy,
                reason
        );
        audit.setStaffId(staff.getId());
        auditRepository.save(audit);

        userRepository.delete(staff);
    }

    public List<User> getAllStaff() {
        return userRepository.findByRole(UserRole.STAFF);
    }
}

