package ingsoft.patrones.visitor;

import java.util.List;

public class ShapeCalculator implements ShapeVisitor {
  public static double getTotalArea(List<Shape> shapes) {
    double totalArea = 0;
    ShapeCalculator calculator = new ShapeCalculator();
    for (Shape shape : shapes) {
      totalArea += shape.accept(calculator);
    }
    return totalArea;
  }

  @Override
  public double visit(Circle circle) {
    return circle.getArea();
  }

  @Override
  public double visit(Rectangle rectangle) {
    return rectangle.getArea();
  }

  @Override
  public double visit(Triangle triangle) {
    return triangle.getArea();
  }
}