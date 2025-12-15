package ingsoft.patrones.abstractFactory;

public class WindowsFactory implements UIFactory {
  @Override
  public Button createButton() {
      return new WindowsButton();
  }

  @Override
  public Window createWindow() {
      return new WindowsWindow();
  }
}
