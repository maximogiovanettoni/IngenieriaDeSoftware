package ingsoft.patrones.templateMethod;

public class HotChocolate extends Beverage {
    @Override
    protected String brew() {
        return brewChocolate();
    }

    private String brewChocolate() {
        return "Mixing chocolate powder\n";
    }

    @Override
    protected String addCondiments() {
        return addMarshmallows();
    }

    private String addMarshmallows() {
        return "Adding marshmallows\n";
    }
}
