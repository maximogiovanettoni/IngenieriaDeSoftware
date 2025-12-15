package ingsoft.patrones.state;

public class FanMedium implements FanState {
    
    @Override
    public String getStateName() {
        return "Medium";
    }

    @Override
    public void turnUp(Fan fan) {
        fan.setState(new FanHigh());
    }

    @Override
    public void turnDown(Fan fan) {
        fan.setState(new FanLow());
    }
    
}
