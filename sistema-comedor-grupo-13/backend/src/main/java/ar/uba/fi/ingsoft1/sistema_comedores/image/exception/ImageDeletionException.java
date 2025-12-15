package ar.uba.fi.ingsoft1.sistema_comedores.image.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;
import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.TechnicalException;

public class ImageDeletionException extends TechnicalException {
    public ImageDeletionException(String message) {
        super(message);
    }
    
    public ImageDeletionException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}