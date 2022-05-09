package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.CostItemRegistrationRequest;
import nl.bd.garage.services.CostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/costitems")
public class CostItemController {
    @Autowired
    private CostItemService costItemService;

    @GetMapping()
    List<CostItem> getAllCostItems() {
        return costItemService.getAllCostItems();
    }

    @GetMapping("/{costItemId}")
    CostItem getCostItemById(@PathVariable long costItemId) {
        return costItemService.getCostItemById(costItemId);
    }

    @Secured({Role.Code.BACKOFFICE, Role.Code.ADMIN})
    @PostMapping()
    CostItem createCostItem(@RequestBody CostItemRegistrationRequest costItemRegistrationRequest) {
        return costItemService.createCostItem(costItemRegistrationRequest);
    }

    @Secured({Role.Code.BACKOFFICE, Role.Code.ADMIN})
    @PutMapping("/{costItemId}")
    CostItem updateCostItem(@RequestBody CostItem costItem, @PathVariable long costItemId) {
        return costItemService.updateCostItem(costItem, costItemId);
    }

    @Secured({Role.Code.BACKOFFICE, Role.Code.ADMIN})
    @PutMapping("/addstock/{costItemId}")
    CostItem addStock(@RequestBody int amount, @PathVariable long costItemId) {
        return costItemService.addStock(amount, costItemId);
    }

    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{costItemId}")
    void deleteCostItem(@PathVariable long costItemId) {
        costItemService.deleteCostItem(costItemId);
    }
}
