package ingsoft.exam.PrimerEjemplo;

public class Number implements Expression {
  private double value;

  public Number(double value) {
    this.value = value;
  }

  @Override
  public double evaluate() {
    return value;
  }

  @Override
  public String getSequenceDiagram() {
    return String.valueOf(value);
  }

  public double getValue() {
    return value;
  }
}
