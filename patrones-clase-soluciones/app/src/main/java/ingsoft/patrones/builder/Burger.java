package ingsoft.patrones.builder;

import java.util.ArrayList;
import java.util.List;

public class Burger implements BurguerPart {
    private List<BurguerPart> parts;

    public Burger(List<BurguerPart> parts) {
        this.parts = new ArrayList<>(parts);
    }

    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        for (BurguerPart part : parts) {
            if (description.length() > 0) {
                description.append("+");
            }
            description.append(part.getDescription());
        }
        return description.toString();
    }

    @Override
    public double getCost() {
        double cost = 0;
        for (BurguerPart part : parts) {
            cost += part.getCost();
        }
        return cost;
    }
}
