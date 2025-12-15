package ingsoft.patrones.abstractFactory;

public class MacFactory implements UIFactory {
  @Override
  public Button createButton() {
      return new MacButton();
  }

  @Override
  public Window createWindow() {
      return new MacWindow();
  }
}
