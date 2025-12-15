package ingsoft.patrones.command;

public class VolumeDown implements Command {
    private Television television;
    private int previousVolume;

    public VolumeDown(Television television) {
        this.television = television;
    }

    @Override
    public void execute() {
        previousVolume = television.getVolume();
        television.setVolume(previousVolume - 1);
    }

    @Override
    public void undo() {
        television.setVolume(previousVolume);
    }
}