package ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateIngredientNameDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    public String name;
}
