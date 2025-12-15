package ingsoft.patrones.strategy;

public class Calculator {
    public int calculate(CalculatingMethod operation, int a, int b) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }
        return operation.calculate(a, b);
    }
}