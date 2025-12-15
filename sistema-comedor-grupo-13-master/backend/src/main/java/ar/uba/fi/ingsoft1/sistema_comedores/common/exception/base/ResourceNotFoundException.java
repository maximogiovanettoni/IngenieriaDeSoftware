package ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base;

public abstract class ResourceNotFoundException extends RuntimeException {
    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
