package ar.uba.fi.ingsoft1.sistema_comedores.user.admin;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "staff_deletion_audit")
public class StaffDeletionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "staff_email")
    private String staffEmail;

    @Column(name = "deleted_by", nullable = false)
    private String deletedBy;

    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt = Instant.now();

    @Column(columnDefinition = "text")
    private String reason;

    public StaffDeletionAudit() {}

    public StaffDeletionAudit(String staffEmail, String deletedBy, String reason) {
        this.staffEmail = staffEmail;
        this.deletedBy = deletedBy;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Long getStaffId() { return staffId; }
    public String getStaffEmail() { return staffEmail; }
    public String getDeletedBy() { return deletedBy; }
    public Instant getDeletedAt() { return deletedAt; }
    public String getReason() { return reason; }

    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public void setStaffEmail(String staffEmail) { this.staffEmail = staffEmail; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
    public void setReason(String reason) { this.reason = reason; }
}
