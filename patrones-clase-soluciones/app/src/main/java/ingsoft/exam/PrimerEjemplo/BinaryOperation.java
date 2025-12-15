package ingsoft.exam.PrimerEjemplo;

public class BinaryOperation implements Expression {
  private Expression left;
  private Expression right;
  private char operator;

  public BinaryOperation(Expression left, char operator, Expression right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public double evaluate() {
    double leftValue = left.evaluate();
    double rightValue = right.evaluate();

    switch (operator) {
      case '+':
        return leftValue + rightValue;
      case '-':
        return leftValue - rightValue;
      case '*':
        return leftValue * rightValue;
      case '/':
        if (rightValue == 0) {
          throw new ArithmeticException("Division by zero");
        }
        return leftValue / rightValue;
      default:
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
  }

  @Override
  public String getSequenceDiagram() {
    return "(" + left.getSequenceDiagram() + " " + operator + " " + right.getSequenceDiagram() + ")";
  }

  public Expression getLeft() {
    return left;
  }

  public Expression getRight() {
    return right;
  }

  public char getOperator() {
    return operator;
  }
}
