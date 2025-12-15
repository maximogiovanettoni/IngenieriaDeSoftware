package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class MustChangePasswordException extends BusinessRuleException {
    public MustChangePasswordException() {
        super("Debe cambiar la contraseña antes de continuar.");
    }

    public MustChangePasswordException(String message) {
        super(message);
    }
}