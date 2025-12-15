package ingsoft.patrones.abstractFactory;

public class Application {
    private UIFactory uiFactory;
    
    public Application(UIFactory uiFactory) {
        this.uiFactory = uiFactory;
    }
    
    public Button getButton() {
        return uiFactory.createButton();
    }

    public Window getWindow() {
        return uiFactory.createWindow();
    }

    public String renderUI() {
        return getButton().render() + " and " + getWindow().render();
    }
}