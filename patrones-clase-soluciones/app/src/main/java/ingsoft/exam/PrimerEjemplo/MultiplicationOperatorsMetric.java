package ingsoft.exam.PrimerEjemplo;

public class MultiplicationOperatorsMetric implements ExpressionMetric {
  @Override
  public int calculate(Expression expression) {
    return countMultiplicationOperators(expression);
  }

  private int countMultiplicationOperators(Expression expression) {
    if (expression instanceof Number) {
      return 0;
    } else if (expression instanceof BinaryOperation) {
      BinaryOperation binOp = (BinaryOperation) expression;
      int count = binOp.getOperator() == '*' ? 1 : 0;
      return count + countMultiplicationOperators(binOp.getLeft()) + 
             countMultiplicationOperators(binOp.getRight());
    }
    return 0;
  }

  @Override
  public String getName() {
    return "Cantidad de operadores de multiplicación";
  }
}
