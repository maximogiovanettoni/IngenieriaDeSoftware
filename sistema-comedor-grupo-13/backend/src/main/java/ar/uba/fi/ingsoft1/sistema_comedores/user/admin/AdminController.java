package ar.uba.fi.ingsoft1.sistema_comedores.user.admin;

import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;
import ar.uba.fi.ingsoft1.sistema_comedores.user.register.UserCreateResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin") 
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/staff")
    public ResponseEntity<List<User>> getAllStaff(Principal principal) {
        try {
            List<User> staffList = adminService.getAllStaff();
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/staff/{email}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable("email") String email,
            @RequestBody(required = false) @Valid DeleteStaffDTO req,
            Principal principal
    ) {
        String reason = req != null ? req.reason() : null;
        String deletedBy = principal.getName();

        try {
            adminService.deleteStaff(email, deletedBy, reason);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }

    @PostMapping("/staff")  
    public ResponseEntity<?> createStaff(
            @Valid @RequestBody CreateStaffDTO req,
            Principal principal
    ) {
        String createdBy = principal.getName();
        try {
            UserCreateResponseDTO newUser = adminService.createStaffUser(req, createdBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Staff user created successfully",
                "user", newUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error interno del servidor"));
        }
    }
}
