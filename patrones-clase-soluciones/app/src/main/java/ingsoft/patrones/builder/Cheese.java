package ingsoft.patrones.builder;

public class Cheese implements BurguerPart {
    @Override
    public String getDescription() {
        return "Cheese";
    }
    
    @Override
    public double getCost() {
        return 2;
    }
}
