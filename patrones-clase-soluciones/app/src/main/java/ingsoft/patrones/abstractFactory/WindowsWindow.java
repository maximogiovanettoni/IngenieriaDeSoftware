package ingsoft.patrones.abstractFactory;

public class WindowsWindow implements Window {
  @Override
  public String render() {
      return "Rendering Windows window with title bar and close button";
  }
}
