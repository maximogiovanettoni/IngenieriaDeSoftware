package ingsoft.patrones.templateMethod;

public class Coffee extends Beverage {

  protected String brew() {
    return brewCoffeeGrinds();
  }

  private String brewCoffeeGrinds() {
    return "Brewing coffee grinds\n";
  }

  protected String addCondiments() {
    return addSugarAndMilk();
  }

 

  private String addSugarAndMilk() {
    return "Adding sugar and milk\n";
  }
}
