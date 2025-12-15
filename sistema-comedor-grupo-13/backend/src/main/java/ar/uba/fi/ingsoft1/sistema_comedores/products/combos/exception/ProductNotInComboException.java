package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.exception;

import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;

public class ProductNotInComboException extends BusinessRuleException {
    private final Long comboId;
    private final String comboName;
    private final Long productId;
    private final String productName;
    
    public ProductNotInComboException(Long comboId, String comboName, Long productId, String productName) {
        super(String.format("El combo '%s' no está compuesto por el producto '%s'", comboName, productName));
        this.comboId = comboId;
        this.comboName = comboName;
        this.productId = productId;
        this.productName = productName;
    }
    
    public ProductNotInComboException(String comboName, String productName) {
        this(null, comboName, null, productName);
    }
    
    public Long getComboId() { return comboId; }
    public String getComboName() { return comboName; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
}