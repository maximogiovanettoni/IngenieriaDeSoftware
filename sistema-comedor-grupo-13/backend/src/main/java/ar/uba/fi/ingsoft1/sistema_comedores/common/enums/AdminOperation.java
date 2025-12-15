package ar.uba.fi.ingsoft1.sistema_comedores.common.enums;

public enum AdminOperation {
    CREATE("Create"),
    ADD_STOCK("Add Stock"),
    UPDATE_STOCK("Update Stock"), 
    CHANGE_NAME("Change Name"),
    CHANGE_PRICE("Change Price"),
    CHANGE_IMAGE("Change Image"),
    DEACTIVATE("Deactivate"),
    REACTIVATE("Reactivate");

    private String description;

    AdminOperation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}