package ingsoft.patrones.state;

public class FanLow implements FanState {
    

    @Override
    public String getStateName() {
        return "Low";
    }

    @Override
    public void turnUp(Fan fan) {
        fan.setState(new FanMedium());
    }

    @Override
    public void turnDown(Fan fan) {
        fan.setState(new FanOff());
    }
}