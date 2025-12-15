package ar.uba.fi.ingsoft1.sistema_comedores.user;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;

public interface UserCredentials {
    String getEmail();
    String getPassword();
    UserRole getRole();
}