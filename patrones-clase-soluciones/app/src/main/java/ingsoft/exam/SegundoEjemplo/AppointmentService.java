package ingsoft.exam.SegundoEjemplo;

public class AppointmentService {

    private PatientType patientType;
    private CommunicationChannel channel;
    private Double fee;

    public void createAppointment() {
        patientType.applyFee(this);
        channel.send();
        System.out.println("Saving appointment to DB...");
        System.out.println("Registering payment of $" + fee);
    }

    public Double getFee() {
        return fee;
    }
    public void setFee(Double fee) {
        this.fee = fee;
    }

}
