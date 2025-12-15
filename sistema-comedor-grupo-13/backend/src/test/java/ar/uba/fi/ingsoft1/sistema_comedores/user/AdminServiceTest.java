package ar.uba.fi.ingsoft1.sistema_comedores.user;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.TokenType;
import ar.uba.fi.ingsoft1.sistema_comedores.user.admin.AdminService;
import ar.uba.fi.ingsoft1.sistema_comedores.user.admin.StaffDeletionAudit;
import ar.uba.fi.ingsoft1.sistema_comedores.user.admin.StaffDeletionAuditRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private StaffDeletionAuditRepository auditRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AdminService adminService;

    private User staffUser;

    private LocalDate birthDate = LocalDate.of(1995, 3, 1);

    @BeforeEach
    void setup() {
        staffUser = new User("staff@fi.uba.ar", "Staff123", "Staff", "uno", "Paseo Colon 850", birthDate, Gender.MALE, UserRole.STAFF);
    }

    @Test
    void deleteStaff_happyPath_invalidatesRefreshTokens_savesAudit_andDeletesUser() {
        when(userRepository.findByEmail("staff@fi.uba.ar")).thenReturn(Optional.of(staffUser));

        adminService.deleteStaff("staff@fi.uba.ar", "admin@fi.uba.ar", "Fin de contrato");

        verify(tokenService).invalidateAllForUser(staffUser, TokenType.REFRESH);
        verify(userRepository).delete(staffUser);
        verify(auditRepository).save(any(StaffDeletionAudit.class));
    }

    @Test
    void deleteStaff_userNotFound_throwsException() {
        when(userRepository.findByEmail("noexiste@fi.uba.ar")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                adminService.deleteStaff("noexiste@fi.uba.ar", "admin@fi.uba.ar", "No existe"));

        verify(userRepository, never()).delete(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    void deleteStaff_invalidRole_throwsIllegalArgument() {
        User student = new User("student@fi.uba.ar", "Student123", "Student", "uno", "Paseo Colon 850", birthDate, Gender.MALE, UserRole.STUDENT);
        student.setId(20L);

        when(userRepository.findByEmail("student@fi.uba.ar")).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () ->
                adminService.deleteStaff("student@fi.uba.ar", "admin@fi.uba.ar", "Rol inválido"));

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteStaff_selfDeletion_throwsIllegalState() {
        when(userRepository.findByEmail("staff@fi.uba.ar")).thenReturn(Optional.of(staffUser));

        assertThrows(IllegalStateException.class, () ->
                adminService.deleteStaff("staff@fi.uba.ar", "staff@fi.uba.ar", "autoelim"));

        verify(userRepository, never()).delete(any());
    }

}