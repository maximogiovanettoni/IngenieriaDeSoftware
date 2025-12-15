package ar.uba.fi.ingsoft1.sistema_comedores.menu;

/**
 * Enum que especifica el tipo de ítem en el menú
 */
public enum MenuItemType {
    PRODUCT("Producto"),
    COMBO("Combo");

    private final String displayName;

    MenuItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
