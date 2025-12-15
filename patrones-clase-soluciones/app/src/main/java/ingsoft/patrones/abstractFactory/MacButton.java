package ingsoft.patrones.abstractFactory;

public class MacButton implements Button {
    @Override
    public String render() {
        return "Rendering Mac button with rounded corners";
    }
}
