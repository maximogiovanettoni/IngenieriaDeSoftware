package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto;

import jakarta.validation.constraints.Size;

public class DeleteIngredientRequest {
    @Size(max = 300, message = "Reason must not exceed 300 characters")
    public String reason;
}
