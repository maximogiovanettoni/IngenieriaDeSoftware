package ar.uba.fi.ingsoft1.sistema_comedores.user.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class AccountNotVerifiedException extends BusinessRuleException {
    public AccountNotVerifiedException(String message) {
        super(message);
    }
}