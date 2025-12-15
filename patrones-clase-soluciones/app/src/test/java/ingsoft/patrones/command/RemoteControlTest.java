package ingsoft.patrones.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RemoteControlTest {
  @Test
  public void testTurnOnTelevision() {
    Television tv = new Television();
    RemoteControl remote = new RemoteControl();
    
    remote.executeCommand(new TurnOn(tv));
    assertTrue(tv.isOn());
  }

  @Test
  public void testTurnOffTelevision() {
    Television tv = new Television();
    RemoteControl remote = new RemoteControl();
    
    remote.executeCommand(new TurnOn(tv));
    assertTrue(tv.isOn());
    
    remote.executeCommand(new TurnOff(tv));
    assertFalse(tv.isOn());
  }

  @Test
  public void testChangeChannel() {
    Television tv = new Television();
    RemoteControl remote = new RemoteControl();
    
    remote.executeCommand(new TurnOn(tv));
    remote.executeCommand(new ChangeChannel(tv, 5));
    
    assertEquals(5, tv.getCurrentChannel());
  }

  @Test
  public void testVolumeControl() {
    Television tv = new Television();
    RemoteControl remote = new RemoteControl();
    
    remote.executeCommand(new TurnOn(tv));
    remote.executeCommand(new VolumeUp(tv));
    remote.executeCommand(new VolumeUp(tv));
    
    assertEquals(12, tv.getVolume()); // Inicia en 10
    
    remote.executeCommand(new VolumeDown(tv));
    assertEquals(11, tv.getVolume());
  }

  @Test
  public void testUndoCommand() {
    Television tv = new Television();
    RemoteControl remote = new RemoteControl();
    
    remote.executeCommand(new TurnOn(tv));
    assertTrue(tv.isOn());
    
    remote.undo();
    assertFalse(tv.isOn());
  }

  @Test
  public void testMultipleUndos() {
    Television tv = new Television();
    RemoteControl remote = new RemoteControl();
    
    remote.executeCommand(new TurnOn(tv));
    remote.executeCommand(new VolumeUp(tv));
    remote.executeCommand(new VolumeUp(tv));
    
    assertEquals(12, tv.getVolume());
    
    remote.undo();
    assertEquals(11, tv.getVolume());
    
    remote.undo();
    assertEquals(10, tv.getVolume());
  }
}
