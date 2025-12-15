package ingsoft.patrones.strategy;

public class Multiply implements CalculatingMethod {
    @Override
    public int calculate(int a, int b) {
        return a * b;
    }

    @Override
    public String getMethod() {
        return "multiply";
    }
}
