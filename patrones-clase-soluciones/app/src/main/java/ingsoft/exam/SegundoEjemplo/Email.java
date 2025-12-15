package ingsoft.exam.SegundoEjemplo;

public class Email implements CommunicationChannel {

    @Override
    public void send() {
        System.out.println("Sending email...");
    }
    
}
