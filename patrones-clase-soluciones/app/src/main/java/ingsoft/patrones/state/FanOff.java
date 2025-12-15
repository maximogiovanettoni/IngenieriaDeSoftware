package ingsoft.patrones.state;

public class FanOff implements FanState {
    @Override
    public String getStateName() {
        return "Off";
    }

    @Override
    public void turnUp(Fan fan) {
        fan.setState(new FanLow());
    }

    @Override
    public void turnDown(Fan fan) {
        // Already off, do nothing or handle accordingly
    }
    
}
