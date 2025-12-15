package ingsoft.patrones.factoryMethod;

public class Lamborgini extends Car {
    public int calculateDistance(int timeAmount) {
        return createVelocityCalculator().getVelocity() * timeAmount;
    }

    @Override
    public VelocityCalculator createVelocityCalculator() {
        return new LamborginiVelocityCalculator();
    }
}
