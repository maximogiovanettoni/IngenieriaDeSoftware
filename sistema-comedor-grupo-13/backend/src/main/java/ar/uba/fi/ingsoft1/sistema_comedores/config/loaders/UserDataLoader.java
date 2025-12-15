package ar.uba.fi.ingsoft1.sistema_comedores.config.loaders;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.ValidationConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.staff.emails:}")
    private String staffEmails;

    @Value("${app.staff.passwords:}")
    private String staffPasswords;

    @Value("${app.student.emails:}")
    private String studentEmails;

    @Value("${app.student.passwords:}")
    private String studentPasswords;

    public UserDataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            createAdminUser();
            createStaffUsers();
            createStudentUsers();
            log.info("✓ User initialization completed");
        } catch (Exception e) {
            log.error("✗ Error initializing users", e);
        }
    }

    private void createAdminUser() {
        createUser(adminEmail, adminPassword, "Admin", "User", UserRole.ADMIN);
    }

    private void createStaffUsers() {
        List<String> emails = parseCommaSeparated(staffEmails);
        List<String> passwords = parseCommaSeparated(staffPasswords);
        
        if (emails.isEmpty()) {
            log.warn("No staff users configured");
            return;
        }
        
        if (emails.size() != passwords.size()) {
            log.error("Staff emails and passwords count mismatch: {} emails, {} passwords", 
                      emails.size(), passwords.size());
            return;
        }
        
        for (int i = 0; i < emails.size(); i++) {
            String email = emails.get(i).trim();
            String password = passwords.get(i).trim();
            createUser(email, password, "Staff", "User " + (i + 1), UserRole.STAFF);
        }
    }

    private void createStudentUsers() {
        List<String> emails = parseCommaSeparated(studentEmails);
        List<String> passwords = parseCommaSeparated(studentPasswords);
        
        if (emails.isEmpty()) {
            log.warn("No student users configured");
            return;
        }
        
        if (emails.size() != passwords.size()) {
            log.error("Student emails and passwords count mismatch: {} emails, {} passwords", 
                      emails.size(), passwords.size());
            return;
        }
        
        for (int i = 0; i < emails.size(); i++) {
            String email = emails.get(i).trim();
            String password = passwords.get(i).trim();
            createUser(email, password, "Student", "User " + (i + 1), UserRole.STUDENT);
        }
    }

    private void createUser(String email, String password, String firstName, String lastName, UserRole role) {
        if (email == null || email.isBlank()) {
            log.warn("Skipping user creation: email is blank");
            return;
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            log.info("✓ {} user already exists: {}", role, email);
            return;
        }
        
        User user = new User(
            email,
            passwordEncoder.encode(password),
            firstName,
            lastName,
            firstName + " Address",
            LocalDate.now().minusYears(ValidationConsts.AGE_MIN_VALUE),
            Gender.OTHER,
            role
        );
        user.setIsActive(true);
        userRepository.save(user);
        log.info("✓ {} user created successfully: {}", role, email);
    }

    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(","))
            .stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
}