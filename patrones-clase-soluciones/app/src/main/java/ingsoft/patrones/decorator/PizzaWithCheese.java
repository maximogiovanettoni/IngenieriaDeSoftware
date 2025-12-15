package ingsoft.patrones.decorator;

public class PizzaWithCheese implements PizzaDecorator {
    private PizzaDecorator pizza;

    public PizzaWithCheese(PizzaDecorator pizza) {
        this.pizza = pizza;
    }

     @Override
    public String getDescription() {
        return pizza.getDescription() + " + Cheese";
    }
    
    @Override
    public double getCost() {
        return pizza.getCost() + 1.5;
    }
}
