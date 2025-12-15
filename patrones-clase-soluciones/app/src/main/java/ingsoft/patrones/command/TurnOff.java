package ingsoft.patrones.command;

public class TurnOff implements Command {
  private Television television;

  public TurnOff(Television television) {
    this.television = television;
  }

  @Override
  public void execute() {
    television.turnOff();
  }

  @Override
  public void undo() {
    television.turnOn();
  }
}