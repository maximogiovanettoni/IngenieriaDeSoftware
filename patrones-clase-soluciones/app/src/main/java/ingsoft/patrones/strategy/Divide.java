package ingsoft.patrones.strategy;

public class Divide implements CalculatingMethod {
    @Override
    public int calculate(int a, int b) {
        if (b != 0) {
            return a / b;
        } else {
            throw new ArithmeticException("Cannot divide by zero");
        }
    }
    @Override
    public String getMethod() {
        return "divide";
    }
}