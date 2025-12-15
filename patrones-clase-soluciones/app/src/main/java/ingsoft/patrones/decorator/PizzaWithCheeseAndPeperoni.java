package ingsoft.patrones.decorator;

public class PizzaWithCheeseAndPeperoni implements PizzaDecorator {
    private PizzaDecorator pizza;
    public PizzaWithCheeseAndPeperoni(PizzaDecorator pizza) {
        this.pizza = pizza;
    }

     @Override
    public String getDescription() {
        return pizza.getDescription() + " + Peperoni";
    }
    
    @Override
    public double getCost() {
        return pizza.getCost() + 2;
    }
}
