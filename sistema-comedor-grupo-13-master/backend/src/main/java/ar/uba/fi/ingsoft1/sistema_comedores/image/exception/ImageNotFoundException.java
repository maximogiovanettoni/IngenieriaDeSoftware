package ar.uba.fi.ingsoft1.sistema_comedores.image.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.ResourceNotFoundException;

public class ImageNotFoundException extends ResourceNotFoundException {
    public ImageNotFoundException(String url) {
        super("Image not found: " + url);
    }
}