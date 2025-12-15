package ar.uba.fi.ingsoft1.sistema_comedores.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateRequestDTO;

import java.time.LocalDate;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCreateRequestDTOTestDTOTest {

    @Autowired
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validUserShouldPassValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni",
                "maxi@fi.uba.ar",
                "SecurePass123",
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidEmailDomainShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni",
                "maxi@gmail.com",
                "SecurePass123",
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void tooShortPasswordShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni",
                "maxi@fi.uba.ar",
                "123",  // Password demasiado corto
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void weakPasswordShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni",
                "maxi@fi.uba.ar",
                "password123",  // Password sin mayúsculas
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidAgeShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni",
                "maxi@fi.uba.ar",
                "SecurePass123",
                LocalDate.of(2035, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidFirstNameWithNumbersShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo123",  // Nombre con números
                "Giovanettoni",
                "maxi@fi.uba.ar",
                "SecurePass123",
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void invalidLastNameWithNumbersShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Maximo",
                "Giovanettoni123",  // Apellido con números
                "maxi@fi.uba.ar",
                "SecurePass123",
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Siempreviva 123, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void missingRequiredFieldsShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "",     // firstName vacío
                "",     // lastName vacío
                "",     // email vacío
                "",     // password vacío
                null,   // age null
                null,   // gender null
                ""     // address vacío
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
        // Verificar que hay múltiples violaciones
        assertThat(violations.size()).isGreaterThan(5);
    }

    @Test
    void nullRequiredFieldsShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                null,   // firstName null
                null,   // lastName null
                null,   // email null
                null,   // password null
                null,   // age null
                null,   // gender null
                null   // address null
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void validUserWithDefaultRoleShouldPassValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "María",
                "García",
                "maria.garcia@fi.uba.ar",
                "SecurePass456",
                LocalDate.of(1995, 1, 3),
                "female",
                "Paseo Colón 850, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
        
        // Verificar que el rol es el especificado
        assertThat(user.getRole()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void validUserWithAccentedNamesShouldPassValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "José",
                "Martínez",
                "jose.martinez@fi.uba.ar",
                "SecurePass789",
                LocalDate.of(1995, 1, 3),
                "male",
                "Av. Las Heras 2214, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidGenderShouldFailValidation() {
        UserCreateRequestDTO user = new UserCreateRequestDTO(
                "Ana",
                "López",
                "ana.lopez@fi.uba.ar",
                "SecurePass321",
                LocalDate.of(1995, 1, 3),
                "InvalidGender", // Género inválido
                "Av. Corrientes 1000, CABA"
        );
        Set<ConstraintViolation<UserCreateRequestDTO>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }
}
