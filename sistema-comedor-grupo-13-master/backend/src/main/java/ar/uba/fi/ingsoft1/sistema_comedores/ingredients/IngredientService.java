package ar.uba.fi.ingsoft1.sistema_comedores.ingredients;

import org.springframework.beans.factory.annotation.Autowired;

import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.CreateIngredientRequest;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.dto.IngredientDetailsResponse;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientAlreadyExistsException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientInProductException;
import ar.uba.fi.ingsoft1.sistema_comedores.ingredients.exception.IngredientNotFoundException;

import org.springframework.stereotype.Service;
import java.util.List;
import java.time.Instant;
import java.math.BigDecimal;
import ar.uba.fi.ingsoft1.sistema_comedores.common.exception.base.BusinessRuleException;
import ar.uba.fi.ingsoft1.sistema_comedores.common.enums.AdminOperation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientService {

    private final IngredientRepository repository;
    private final IngredientAuditLogRepository auditRepo;

    @Autowired
    public IngredientService(IngredientRepository repository, IngredientAuditLogRepository auditRepo) {
        this.repository = repository;
        this.auditRepo = auditRepo;
    }

    public List<IngredientDetailsResponse> getAll() {
        return repository.findByActiveTrue().stream().map(IngredientDetailsResponse::from).toList();
    }

    public Ingredient createIngredient(CreateIngredientRequest dto) {
        Ingredient ingredient = dto.toIngredient();
        repository.findByNameAndActiveTrue(ingredient.getName()).ifPresent(i -> {
            throw new IngredientAlreadyExistsException(ingredient.getName());
        });
        var opt = repository.findByName(ingredient.getName());
        if (opt.isPresent()) {
            Ingredient existing = opt.get();
            if (!existing.isActive()) {
                BigDecimal incoming = ingredient.getStock() == null ? BigDecimal.ZERO : ingredient.getStock();
                existing.setActive(true);
                existing.updateStockAndNotify(incoming);
                existing.setAvailable(existing.getStock() != null && existing.getStock().compareTo(BigDecimal.ZERO) > 0);
                existing.notifyObserversStatusChange();
                Ingredient saved = repository.save(existing);

                IngredientAuditLog log = new IngredientAuditLog();
                log.setIngredientName(saved.getName());
                log.setOperation(AdminOperation.REACTIVATE);
                log.setModifiedAt(Instant.now());
                log.setAmountDelta(incoming);
                auditRepo.save(log);

                return saved;
            }
        }

        ingredient.setAvailable(ingredient.isActive() && (ingredient.getStock() != null && ingredient.getStock().compareTo(BigDecimal.ZERO) > 0));
        Ingredient saved = repository.save(ingredient);

        IngredientAuditLog log = new IngredientAuditLog();
        log.setIngredientName(saved.getName());
        log.setOperation(AdminOperation.CREATE);
        log.setModifiedAt(Instant.now());
        log.setAmountDelta(saved.getStock());
        auditRepo.save(log);

        return saved;
    }

    public Ingredient changeName(Long id, String newName) throws IngredientNotFoundException {
        Ingredient ingredient = repository.findById(id).orElseThrow(() -> new IngredientNotFoundException(id));
        if (newName != null && !newName.equalsIgnoreCase(ingredient.getName())) {
            repository.findByNameAndActiveTrue(newName).ifPresent(existing -> {
                if (!existing.getName().equalsIgnoreCase(ingredient.getName())) throw new IngredientAlreadyExistsException(newName);
            });

            String oldName = ingredient.getName();
            ingredient.setName(newName);
            Ingredient saved = repository.save(ingredient);

            IngredientAuditLog log = new IngredientAuditLog();
            log.setIngredientName(saved.getName());
            log.setOperation(AdminOperation.CHANGE_NAME);
            log.setModifiedAt(Instant.now());
            log.setReason("previousName=" + oldName);
            auditRepo.save(log);

            return saved;
        }
        return ingredient;
    }


    public IngredientDetailsResponse deactivateIngredient(Long id, String reason) throws IngredientNotFoundException, IngredientInProductException {
        Ingredient ingredient = repository.findById(id).orElseThrow(() -> new IngredientNotFoundException(id));
        if (!ingredient.isActive()) {
            return IngredientDetailsResponse.from(ingredient);
        }
        if (!ingredient.getObservers().isEmpty()) {
            throw new IngredientInProductException(ingredient.getId(), ingredient.getName());
        }

        ingredient.updateStockAndNotify(BigDecimal.ZERO);
        ingredient.setActive(false);
        ingredient.setAvailable(false);
        ingredient.notifyObserversStatusChange();
        repository.save(ingredient);

        IngredientAuditLog log = new IngredientAuditLog();
        log.setIngredientName(ingredient.getName());
        log.setOperation(AdminOperation.DEACTIVATE);
        log.setModifiedAt(Instant.now());
        log.setReason(reason);
        auditRepo.save(log);

        return IngredientDetailsResponse.from(ingredient);
    }

    @Transactional
    public Ingredient updateStock(Long id, BigDecimal newStock) throws IngredientNotFoundException {
        Ingredient ingredient = repository.findById(id).orElseThrow(() -> new IngredientNotFoundException(id));
        
        BigDecimal oldStock = ingredient.getStock() == null ? BigDecimal.ZERO : ingredient.getStock();
        ingredient.updateStockAndNotify(newStock);
        ingredient.setAvailable(ingredient.isActive() && (ingredient.getStock() != null && ingredient.getStock().compareTo(BigDecimal.ZERO) > 0));
        Ingredient saved = repository.save(ingredient);

        IngredientAuditLog log = new IngredientAuditLog();
        log.setIngredientName(saved.getName());
        log.setOperation(AdminOperation.UPDATE_STOCK);
        log.setModifiedAt(Instant.now());
        log.setAmountDelta(newStock.subtract(oldStock)); 
        auditRepo.save(log);

        return saved;
    }
}
