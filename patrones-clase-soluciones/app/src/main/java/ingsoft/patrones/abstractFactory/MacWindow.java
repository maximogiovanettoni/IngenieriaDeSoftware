package ingsoft.patrones.abstractFactory;

public class MacWindow implements Window {
  @Override
  public String render() {
      return "Rendering Mac window with title bar and close button";
  }
}