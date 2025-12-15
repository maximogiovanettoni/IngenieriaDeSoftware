package ingsoft.patrones.abstractFactory;

public class WindowsButton implements Button {
  @Override
  public String render() {
      return "Rendering Windows button with square corners";
  }
}