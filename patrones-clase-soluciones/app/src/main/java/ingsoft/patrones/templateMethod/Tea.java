package ingsoft.patrones.templateMethod;

public class Tea extends Beverage {
  protected String brew() {
    return steepTeaBag();
  }

  protected String addCondiments() {
    return addLemon();
  }

  private String steepTeaBag() {
    return "Steeping the tea\n";
  }

  private String addLemon() {
    return "Adding lemon\n";
  }
}
