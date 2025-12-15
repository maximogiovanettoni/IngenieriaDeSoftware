package ingsoft.patrones.command;

public class Television {
  private boolean isOn = false;
  private int currentChannel = 1;
  private int volume = 10;

  public boolean isOn() {
    return isOn;
  }

  public void turnOn() {
    isOn = true;
  }

  public void turnOff() {
    isOn = false;
  }

  public int getCurrentChannel() {
    return currentChannel;
  }

  public void setCurrentChannel(int channel) {
    currentChannel = channel;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }
}