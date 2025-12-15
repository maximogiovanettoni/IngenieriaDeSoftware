package ingsoft.patrones.command;

public class TurnOn implements Command {
  private Television television;

  public TurnOn(Television television) {
    this.television = television;
  }

  @Override
  public void execute() {
    television.turnOn();
  }

  @Override
  public void undo() {
    television.turnOff();
  }
}