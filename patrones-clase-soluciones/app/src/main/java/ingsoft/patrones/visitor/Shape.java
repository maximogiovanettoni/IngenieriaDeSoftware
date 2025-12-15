package ingsoft.patrones.visitor;

public interface Shape {
  double getArea();
  double accept(ShapeVisitor visitor);
}
