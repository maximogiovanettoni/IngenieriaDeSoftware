package ar.uba.fi.ingsoft1.sistema_comedores.config.email.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.TechnicalException;

public class FailedToSendEmailException extends TechnicalException {
    public FailedToSendEmailException(String subject, Throwable cause) {
        super("Failed to send email with subject: " + subject, cause);
    }
}
