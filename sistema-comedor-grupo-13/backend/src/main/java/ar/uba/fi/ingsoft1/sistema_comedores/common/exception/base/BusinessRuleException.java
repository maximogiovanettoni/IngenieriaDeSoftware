package ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base;

public abstract class BusinessRuleException extends RuntimeException {
    protected BusinessRuleException(String message) {
        super(message);
    }
}
