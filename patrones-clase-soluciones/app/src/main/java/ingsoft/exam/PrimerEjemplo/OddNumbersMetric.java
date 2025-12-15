package ingsoft.exam.PrimerEjemplo;

public class OddNumbersMetric implements ExpressionMetric {
  @Override
  public int calculate(Expression expression) {
    return countOddNumbers(expression);
  }

  private int countOddNumbers(Expression expression) {
    if (expression instanceof Number) {
      double value = ((Number) expression).getValue();
      // Check if it's an integer and odd
      if (value == Math.floor(value) && ((long) value) % 2 != 0) {
        return 1;
      }
      return 0;
    } else if (expression instanceof BinaryOperation) {
      BinaryOperation binOp = (BinaryOperation) expression;
      return countOddNumbers(binOp.getLeft()) + countOddNumbers(binOp.getRight());
    }
    return 0;
  }

  @Override
  public String getName() {
    return "Cantidad de números impares";
  }
}
