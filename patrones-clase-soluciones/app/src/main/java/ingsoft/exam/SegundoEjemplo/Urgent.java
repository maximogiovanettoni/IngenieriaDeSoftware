package ingsoft.exam.SegundoEjemplo;

public class Urgent implements PatientType {

    @Override
    public void applyFee(AppointmentService appointmentService) {
        appointmentService.setFee(appointmentService.getFee() + 50);
    }
    
}
