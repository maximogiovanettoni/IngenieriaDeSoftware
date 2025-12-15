package ingsoft.patrones.state;

interface FanState {
    String getStateName();
    void turnUp(Fan fan);
    void turnDown(Fan fan);
}
