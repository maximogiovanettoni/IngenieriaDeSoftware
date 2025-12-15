package ingsoft.exam.SegundoEjemplo;

public class Normal implements PatientType {

    @Override
    public void applyFee(AppointmentService appointmentService) {
        appointmentService.setFee(appointmentService.getFee() + 20);
    }
    
}
