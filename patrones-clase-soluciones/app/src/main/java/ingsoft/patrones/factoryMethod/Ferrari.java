package ingsoft.patrones.factoryMethod;

public class Ferrari extends Car {
     @Override
     public VelocityCalculator createVelocityCalculator() {
         return new FerrariVelocityCalculator();
     }
    public int calculateDistance(int timeAmount) {
        return createVelocityCalculator().getVelocity() * timeAmount;
    }
}
