package ingsoft.patrones.command;

public class ChangeChannel implements Command {
    private Television television;
    private int previousChannel;
    private int newChannel;

    public ChangeChannel(Television television, int newChannel) {
        this.television = television;
        this.newChannel = newChannel;
    }

    @Override
    public void execute() {
        previousChannel = television.getCurrentChannel();
        television.setCurrentChannel(newChannel);
    }

    @Override
    public void undo() {
        television.setCurrentChannel(previousChannel);
    }
}