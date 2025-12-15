package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception;

import java.math.BigDecimal;
import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.ValidationException;

public class InvalidComboPriceException extends ValidationException {
    private final BigDecimal comboPrice;
    private final BigDecimal regularPrice;
    
    public InvalidComboPriceException(BigDecimal comboPrice, BigDecimal regularPrice) {
        super(String.format(
            "El precio del combo ($%.2f) no puede ser mayor al precio regular ($%.2f)",
            comboPrice, regularPrice
        ));
        this.comboPrice = comboPrice;
        this.regularPrice = regularPrice;
    }
    
    public BigDecimal getComboPrice() {
        return comboPrice;
    }
    
    public BigDecimal getRegularPrice() {
        return regularPrice;
    }
}