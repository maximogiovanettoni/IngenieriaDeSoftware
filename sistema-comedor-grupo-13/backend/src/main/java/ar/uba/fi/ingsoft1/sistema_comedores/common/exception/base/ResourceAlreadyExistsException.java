package ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base;

public abstract class ResourceAlreadyExistsException extends RuntimeException {
    protected ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
