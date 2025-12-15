package ar.uba.fi.ingsoft1.sistema_comedores.user.admin;

import java.time.LocalDate;
import java.util.function.Function;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;
import ar.uba.fi.ingsoft1.sistema_comedores.config.consts.AdminConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidBirthDate;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidEmail;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidGender;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidName;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidPassword;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserInformation;

public record CreateStaffDTO(

        @ValidName(ValidName.NameType.FIRST_NAME)
        String firstName,
        
        @ValidName(ValidName.NameType.LAST_NAME)
        String lastName,
        
        @ValidEmail
        String email,
        
        @ValidPassword
        String temporaryPassword,
        
        @ValidBirthDate
        LocalDate birthDate,
        
        @ValidGender
        String gender
) implements UserInformation {
        
        @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return temporaryPassword;
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
    public Integer getAge() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public String getAddress() {
        return AdminConsts.adminAddress;
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
    public UserRole getRole() {
        return UserRole.STAFF;
    }
    
    public User asUser(Function<String, String> encryptPassword) {
        return new User(email, encryptPassword.apply(temporaryPassword), firstName, lastName, getAddress(), birthDate, getGender(), getRole());
    }
}
