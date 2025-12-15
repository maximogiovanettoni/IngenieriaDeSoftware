package ar.uba.fi.ingsoft1.sistema_comedores.config.validation;

public class ValidationConsts {

    // Password constants
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 64;
    public static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$";
    
    // Name constants
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 50;
    public static final String NAME_PATTERN = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    
    // Age constants
    public static final int AGE_MIN_VALUE = 18;
    public static final int AGE_MAX_VALUE = 99;
    
    // Address constants
    public static final int ADDRESS_MAX_LENGTH = 100;
    public static final String ADDRESS_PATTERN = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s,.-]+$";
    
    // Email constants
    public static final String ALLOWED_EMAIL_DOMAIN = "@fi.uba.ar";
    public static final String EMAIL_DOMAIN_PATTERN = "^[a-zA-Z0-9._%+-]+@fi\\.uba\\.ar$";
    
    // Gender constants
    public static final String GENDER_PATTERN = "^(male|female|other)$";
    
    // Role constants (added this for your role field)
    public static final String ROLE_PATTERN = "^(admin|student|staff|ADMIN|STUDENT|STAFF)?$";
    
}