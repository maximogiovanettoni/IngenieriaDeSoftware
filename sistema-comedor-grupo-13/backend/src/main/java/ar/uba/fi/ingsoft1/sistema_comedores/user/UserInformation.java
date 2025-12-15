package ar.uba.fi.ingsoft1.sistema_comedores.user;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;

public interface UserInformation extends UserCredentials {
    String getFirstName();
    String getLastName();
    Integer getAge();
    String getAddress();
    Gender getGender();
}