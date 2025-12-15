package ar.uba.fi.ingsoft1.sistema_comedores.image.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class ImageUploadException extends BusinessRuleException {
    
    public ImageUploadException(Throwable cause) {
        super("Failed to upload image to storage");
        initCause(cause);
    }
}