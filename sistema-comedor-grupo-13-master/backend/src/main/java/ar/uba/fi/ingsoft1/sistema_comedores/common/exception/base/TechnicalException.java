package ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base;

public abstract class TechnicalException extends RuntimeException {
    protected TechnicalException(String message) {
        super(message);
    }
    
    protected TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}