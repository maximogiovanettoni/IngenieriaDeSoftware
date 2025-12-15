package ingsoft.patrones.state;

public class Fan {
  private FanState state = new FanOff();
  public void turnUp() {
      state.turnUp(this);
  }

  public void turnDown() {
          state.turnDown(this);
      }
  

  public String getState() {
        return state.getStateName();
  }

    public void setState(FanState state) {
        this.state = state;
    }
}