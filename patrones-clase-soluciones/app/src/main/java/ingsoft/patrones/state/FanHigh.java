package ingsoft.patrones.state;

public class FanHigh implements FanState {
    
    @Override
    public String getStateName() {
        return "High";
    }

    @Override
    public void turnUp(Fan fan) {
        // Already at high, do nothing or handle accordingly
    }

    @Override
    public void turnDown(Fan fan) {
        fan.setState(new FanMedium());
    }
}