package ingsoft.patrones.strategy;

public class Add implements CalculatingMethod {
    @Override
    public int calculate(int a, int b) {
        return a + b;
    }
    @Override
    public String getMethod() {
        return "add";
    }
    
}
