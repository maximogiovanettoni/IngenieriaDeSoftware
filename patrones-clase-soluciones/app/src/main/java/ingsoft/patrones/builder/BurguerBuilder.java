package ingsoft.patrones.builder;

import java.util.ArrayList;
import java.util.List;

public class BurguerBuilder {
    private List<BurguerPart> parts;

    public BurguerBuilder() {
        this.parts = new ArrayList<>();
    }

    public BurguerBuilder addBread() {
        parts.add(new Bread());
        return this;
    }

    public BurguerBuilder addMeet() {
        parts.add(new Meet());
        return this;
    }

    public BurguerBuilder addCheese() {
        parts.add(new Cheese());
        return this;
    }

    public Burger build() {
        return new Burger(parts);
    }
}
