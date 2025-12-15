package ingsoft.exam.PrimerEjemplo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExpressionTest {
  @Test
  public void testSimpleNumber() {
    Expression expr = new Number(5);
    assertEquals(5, expr.evaluate());
  }

  @Test
  public void testAddition() {
    Expression expr = new BinaryOperation(new Number(2), '+', new Number(4));
    assertEquals(6, expr.evaluate());
  }

  @Test
  public void testSubtraction() {
    Expression expr = new BinaryOperation(new Number(5), '-', new Number(3));
    assertEquals(2, expr.evaluate());
  }

  @Test
  public void testMultiplication() {
    Expression expr = new BinaryOperation(new Number(3), '*', new Number(4));
    assertEquals(12, expr.evaluate());
  }

  @Test
  public void testDivision() {
    Expression expr = new BinaryOperation(new Number(8), '/', new Number(2));
    assertEquals(4, expr.evaluate());
  }

  @Test
  public void testComplexExpression1() {
    // ((2 + 4) * 5 - 3) / 2
    Expression expr = new BinaryOperation(
      new BinaryOperation(
        new BinaryOperation(
          new BinaryOperation(new Number(2), '+', new Number(4)),
          '*',
          new Number(5)
        ),
        '-',
        new Number(3)
      ),
      '/',
      new Number(2)
    );
    assertEquals(13.5, expr.evaluate());
  }

  @Test
  public void testComplexExpression2() {
    // 4 * (7 - 4) / 2 + 6
    Expression expr = new BinaryOperation(
      new BinaryOperation(
        new BinaryOperation(new Number(4), '*', new BinaryOperation(new Number(7), '-', new Number(4))),
        '/',
        new Number(2)
      ),
      '+',
      new Number(6)
    );
    assertEquals(12, expr.evaluate());
  }

  @Test
  public void testSequenceDiagram() {
    Expression expr = new BinaryOperation(new Number(2), '+', new Number(3));
    assertEquals("(2.0 + 3.0)", expr.getSequenceDiagram());
  }

  @Test
  public void testOddNumbersMetric() {
    // Expression: 4 * (7 - 4) / 2 + 6
    // Odd numbers: 7
    Expression expr = new BinaryOperation(
      new BinaryOperation(
        new BinaryOperation(new Number(4), '*', new BinaryOperation(new Number(7), '-', new Number(4))),
        '/',
        new Number(2)
      ),
      '+',
      new Number(6)
    );

    ExpressionMetric metric = new OddNumbersMetric();
    assertEquals(1, metric.calculate(expr));
    assertEquals("Cantidad de números impares", metric.getName());
  }

  @Test
  public void testMultiplicationMetric() {
    // Expression: 4 * (7 - 4) / 2 + 6
    // Multiplication operators: 1 (the *)
    Expression expr = new BinaryOperation(
      new BinaryOperation(
        new BinaryOperation(new Number(4), '*', new BinaryOperation(new Number(7), '-', new Number(4))),
        '/',
        new Number(2)
      ),
      '+',
      new Number(6)
    );

    ExpressionMetric metric = new MultiplicationOperatorsMetric();
    assertEquals(1, metric.calculate(expr));
    assertEquals("Cantidad de operadores de multiplicación", metric.getName());
  }

  @Test
  public void testComplexExpressionWithMultipleMetrics() {
    // Expression: ((2 + 4) * 5 - 3) / 2
    // Odd numbers: 5, 3
    // Multiplication operators: 1
    Expression expr = new BinaryOperation(
      new BinaryOperation(
        new BinaryOperation(
          new BinaryOperation(new Number(2), '+', new Number(4)),
          '*',
          new Number(5)
        ),
        '-',
        new Number(3)
      ),
      '/',
      new Number(2)
    );

    ExpressionMetric oddMetric = new OddNumbersMetric();
    ExpressionMetric multMetric = new MultiplicationOperatorsMetric();

    assertEquals(2, oddMetric.calculate(expr));
    assertEquals(1, multMetric.calculate(expr));
  }
}
