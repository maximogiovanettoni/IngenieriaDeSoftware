package ar.uba.fi.ingsoft1.sistema_comedores.products.combos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import ar.uba.fi.ingsoft1.sistema_comedores.products.combos.Combo;

public record CreateComboRequest(
        @NotBlank(message = "El nombre del combo es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @Size(max = 300, message = "La descripción no puede exceder 300 caracteres")
        String description,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser positivo")
        BigDecimal price,

        @NotNull
        Boolean active,
        
        @NotNull(message = "Los productos son obligatorios")
        @Size(min = 2, message = "El combo debe tener al menos dos productos")
        Map<Long, @Positive(message = "La cantidad debe ser mayor a 0") Integer> products
) {
        public Combo toCombo() {
                return new Combo(name(), description(), price(), active());
        }
}