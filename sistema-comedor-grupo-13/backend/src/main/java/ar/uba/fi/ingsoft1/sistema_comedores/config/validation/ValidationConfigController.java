package ar.uba.fi.ingsoft1.sistema_comedores.config.validation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Validation Config")
@RequestMapping("/config")
public class ValidationConfigController {

    private final ValidationConfig validationConfig;

    public ValidationConfigController(ValidationConfig validationConfig) {
        this.validationConfig = validationConfig;
    }

    @GetMapping("/validation")
    @Operation(summary = "Provides validation rules for client awareness")
    public ValidationConfig getValidationConfig() {
        return validationConfig;
    }
}
