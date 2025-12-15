package ingsoft.patrones.decorator;

public class Pizza implements PizzaDecorator {
     @Override
    public String getDescription() {
        return "Pizza";
    }
    
    @Override
    public double getCost() {
        return 10;
    }
}
