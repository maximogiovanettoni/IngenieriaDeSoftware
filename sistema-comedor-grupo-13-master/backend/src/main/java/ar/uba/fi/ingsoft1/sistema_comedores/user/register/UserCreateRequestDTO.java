package ar.uba.fi.ingsoft1.sistema_comedores.user.register;

import java.time.LocalDate;
import java.util.function.Function;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.*;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserInformation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for creating a new user")
public record UserCreateRequestDTO(
        @Schema(description = "User's first name", example = "Juan")
        @ValidName(ValidName.NameType.FIRST_NAME)
        String firstName,
        
        @Schema(description = "User's last name", example = "Pérez")
        @ValidName(ValidName.NameType.LAST_NAME)
        String lastName,
        
        @Schema(description = "User's institutional email address", example = "juan.perez@fi.uba.ar")
        @ValidEmail
        String email,
        
        @Schema(description = "User's password (minimum 8 characters, must contain uppercase, lowercase and digit)", 
                example = "SecurePass123", minLength = 8, maxLength = 64)
        @ValidPassword
        String password,
        
        @Schema(description = "User's birth date (must be 18-99 years old)", example = "1995-03-15")
        @ValidBirthDate
        LocalDate birthDate,
        
        @Schema(description = "User's gender", example = "male", allowableValues = {"male", "female", "other"})
        @ValidGender
        String gender,
        
        @Schema(description = "User's address", example = "Av. Paseo Colón 850, CABA")
        @ValidAddress
        String address
        
) implements UserInformation {
    
    @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    @Schema(hidden = true)
    public Integer getAge() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public String getAddress() {
        return address;
    }
    
    @Override
    public Gender getGender() {
        try {
            return Gender.fromValue(gender);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid gender value: " + gender, e);
        }
    }

    @Override
    @Schema(hidden = true)
    public UserRole getRole() {
        return UserRole.STUDENT;
    }
    
    public User asUser(Function<String, String> encryptPassword) {
        return new User(email, encryptPassword.apply(password), firstName, lastName, address, birthDate, getGender(), getRole());
    }
}