package ingsoft.patrones.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class CalculatorTest {
  private final Calculator calculator = new Calculator();
  private final Add add = new Add();
  private final Substract substract = new Substract();
  private final Multiply multiply = new Multiply();
  private final Divide divide = new Divide();

  @Test
  void testAddition() {
      assertEquals(5, calculator.calculate(add, 2, 3));
  }

  @Test
  void testSubtraction() {
      assertEquals(2, calculator.calculate(substract, 5, 3));
  }

  @Test
  void testMultiplication() {
      assertEquals(15, calculator.calculate(multiply, 3, 5));
  }

  @Test
  void testDivision() {
      assertEquals(2, calculator.calculate(divide, 6, 3));
  }

  @Test
  void testDivisionByZero() {
      assertThrows(ArithmeticException.class, () -> calculator.calculate(divide, 6, 0));
  }

  @Test
  void testUnknownOperation() {
      assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null, 2, 3));
  }
}
