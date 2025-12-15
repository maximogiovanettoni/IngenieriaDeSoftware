package ingsoft.patrones.visitor;

public class Circle implements Shape {
  private double radius;

  public Circle(double radius) {
    this.radius = radius;
  }

  @Override
  public double getArea() {
    return Math.PI * radius * radius;
  }

  @Override
  public double accept(ShapeVisitor visitor) {
    return visitor.visit(this);
  }
}
