package ingsoft.exam.SegundoEjemplo;

public class Sms implements CommunicationChannel {

    @Override
    public void send() {
        System.out.println("Sending SMS...");
    }
    
}
