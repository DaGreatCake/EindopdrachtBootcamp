package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.CostItemRegistrationRequest;
import nl.bd.garage.services.CostItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Handles http requests on endpoints concerning cost items.
 */
@RestController
@RequestMapping("api/costitems")
public class CostItemController {
    @Autowired
    private CostItemService costItemService;

    /*
     * Returns all cost items.
     */
    @GetMapping()
    List<CostItem> getAllCostItems() {
        return costItemService.getAllCostItems();
    }

    /*
     * Gets a cost item by id.
     *
     * Provide cost item id in the url.
     */
    @GetMapping("/{costItemId}")
    CostItem getCostItemById(@PathVariable long costItemId) {
        return costItemService.getCostItemById(costItemId);
    }

    /*
     * Creates a new cost item.
     *
     * Provide name:String, cost:double, costType:CostType in the body.
     */
    @Secured(Role.Code.BACKOFFICE)
    @PostMapping()
    CostItem createCostItem(@RequestBody CostItemRegistrationRequest costItemRegistrationRequest) {
        return costItemService.createCostItem(costItemRegistrationRequest);
    }

    /*
     * Updates details of cost item.
     *
     * Provide cost item id in the url. Provide name:String, cost:double, costType:CostType in the body.
     */
    @Secured(Role.Code.BACKOFFICE)
    @PutMapping("/{costItemId}")
    CostItem updateCostItem(@RequestBody CostItemRegistrationRequest costItemRegistrationRequest,
                            @PathVariable long costItemId) {
        return costItemService.updateCostItem(costItemRegistrationRequest, costItemId);
    }

    /*
     * Adds stock to a cost item.
     *
     * Provide cost item id in the url. Provide amount:int in the body.
     */
    @Secured(Role.Code.BACKOFFICE)
    @PutMapping("/addstock/{costItemId}")
    CostItem addStock(@RequestBody int amount, @PathVariable long costItemId) {
        return costItemService.addStock(amount, costItemId);
    }

    /*
     * Delete a cost item.
     *
     * Provide cost item id in the url.
     */
    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{costItemId}")
    void deleteCostItem(@PathVariable long costItemId) {
        costItemService.deleteCostItem(costItemId);
    }
}
