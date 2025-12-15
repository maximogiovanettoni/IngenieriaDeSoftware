package ingsoft.patrones.builder;

public class Bread implements BurguerPart {
    @Override
    public String getDescription() {
        return "Bread";
    }
    
    @Override
    public double getCost() {
        return 1;
    }
}
