package ar.uba.fi.ingsoft1.sistema_comedores.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import ar.uba.fi.ingsoft1.sistema_comedores.config.validation.annotations.ValidBirthDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.Gender;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.UserRole;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User implements UserDetails, UserInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @NotNull
    @Past
    @ValidBirthDate
    private LocalDate birthDate;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = true)
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isActive = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", nullable = true)
    private String createdBy;

    @Column(name = "must_change_password", nullable = false, columnDefinition = "boolean default false")
    private Boolean mustChangePassword = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isActive == null) {
            isActive = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructor
    public User(String email, String password, String firstName, String lastName, 
        String address, LocalDate birthDate, Gender gender, UserRole role) {
        this.email = email;
        this.username = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
        this.isActive = false;
    }

    public Integer getAge() {
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getValue()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public void setGender(String genderValue) {
        this.gender = Gender.fromValue(genderValue);
    }
}