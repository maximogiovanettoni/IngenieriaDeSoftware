package ingsoft.patrones.abstractFactory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UIFactoryTest {
  @Test
  public void testWindowsFactory() {
    UIFactory factory = new WindowsFactory();
    Application app = new Application(factory);

    assertTrue(app.getButton() instanceof WindowsButton);
    assertTrue(app.getWindow() instanceof WindowsWindow);
    
    String ui = app.renderUI();
    assertTrue(ui.contains("Windows button"));
    assertTrue(ui.contains("Windows window"));
  }

  @Test
  public void testMacFactory() {
    UIFactory factory = new MacFactory();
    Application app = new Application(factory);

    assertTrue(app.getButton() instanceof MacButton);
    assertTrue(app.getWindow() instanceof MacWindow);
    
    String ui = app.renderUI();
    assertTrue(ui.contains("Mac button"));
    assertTrue(ui.contains("Mac window"));
  }

  @Test
  public void testWindowsButtonRender() {
    Button button = new WindowsButton();
    assertEquals("Rendering Windows button with squared corners", button.render());
  }

  @Test
  public void testMacButtonRender() {
    Button button = new MacButton();
    assertEquals("Rendering Mac button with rounded corners", button.render());
  }

  @Test
  public void testWindowsWindowRender() {
    Window window = new WindowsWindow();
    assertEquals("Rendering Windows window with title bar and system buttons", window.render());
  }

  @Test
  public void testMacWindowRender() {
    Window window = new MacWindow();
    assertEquals("Rendering Mac window with metal frame and traffic lights", window.render());
  }
}
