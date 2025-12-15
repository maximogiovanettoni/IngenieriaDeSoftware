package ingsoft.patrones.builder;

public class Meet implements BurguerPart {
    @Override
    public String getDescription() {
        return "Meet";
    }
    
    @Override
    public double getCost() {
        return 5;
    }
}
