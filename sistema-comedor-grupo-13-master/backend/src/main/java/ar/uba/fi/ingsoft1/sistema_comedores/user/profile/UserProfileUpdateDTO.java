package ar.uba.fi.ingsoft1.sistema_comedores.user.profile;

import java.time.LocalDate;

import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidAddress;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidBirthDate;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidEmail;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidGender;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidName;

public record UserProfileUpdateDTO(
        @ValidName(ValidName.NameType.FIRST_NAME)
        String firstName,
        
        @ValidName(ValidName.NameType.LAST_NAME)
        String lastName,
        
        @ValidEmail
        String email,
        
        @ValidBirthDate
        LocalDate birthDate,
        
        @ValidGender
        String gender,
        
        @ValidAddress
        String address
) {}
