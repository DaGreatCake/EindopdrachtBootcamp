package nl.bd.garage.services;

import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.enums.CostType;
import nl.bd.garage.models.exceptions.AddStockToActionException;
import nl.bd.garage.models.exceptions.CostItemNotFoundException;
import nl.bd.garage.models.requests.CostItemRegistrationRequest;
import nl.bd.garage.repositories.CostItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * This service will handle all the functionality that is being requested through CostItemController and interact with
 * the database accordingly.
 */
@Service
public class CostItemService {
    @Autowired
    private CostItemRepository costItemRepository;

    // Returns all cost items.
    public List<CostItem> getAllCostItems() {
        return costItemRepository.findAll();
    }

    /*
     * Returns a cost item by id.
     *
     * Will throw exception if cost item id doesnt exist.
     */
    public CostItem getCostItemById(Long costItemId) {
        return costItemRepository.findById(costItemId).orElseThrow(() -> new CostItemNotFoundException(costItemId));
    }

    // Creates a new cost item in the database.
    public CostItem createCostItem(CostItemRegistrationRequest costItemRegistrationRequest) {
        CostItem costItem = new CostItem(costItemRegistrationRequest.getName(), costItemRegistrationRequest.getCost(),
                costItemRegistrationRequest.getCostType());
        return costItemRepository.save(costItem);
    }

    /*
     * Updates the details of an existing cost item. If the type is changed to ACTION. The stock will be set to -1.
     * If an item is changed from ACTION to PART, the stock will be set to 0.
     *
     * Will throw exception if the cost item id doesnt exist.
     */
    public CostItem updateCostItem(CostItemRegistrationRequest newCostItem, Long costItemId) {
        return costItemRepository.findById(costItemId)
                .map(costItem -> {
                    costItem.setName(newCostItem.getName());
                    costItem.setCost(newCostItem.getCost());
                    costItem.setCostType(newCostItem.getCostType());

                    if (costItem.getCostType() == CostType.ACTION) {
                        costItem.setStock(-1);
                    } else if (costItem.getStock() == -1) {
                        costItem.setStock(0);
                    }

                    return costItemRepository.save(costItem);
                })
                .orElseThrow(() -> new CostItemNotFoundException(costItemId));
    }

    /*
     * Adds stock to a cost item.
     *
     * Will throw exception if cost item is of type ACTION or if cost item id doesnt exist.
     */
    public CostItem addStock(int amount, Long costItemId) {
        return costItemRepository.findById(costItemId)
                .map(costItem -> {
                    if (costItem.getCostType() == CostType.PART) {
                        costItem.setStock(costItem.getStock() + amount);
                    } else {
                        throw new AddStockToActionException(costItemId);
                    }
                    return costItemRepository.save(costItem);
                })
                .orElseThrow(() -> new CostItemNotFoundException(costItemId));
    }

    /*
     * Deletes a cost item from the database.
     *
     * Will throw exception if cost item id doesnt exist.
     */
    public void deleteCostItem(Long costItemId) {
        if (costItemRepository.findById(costItemId).isPresent()) {
            costItemRepository.deleteById(costItemId);
        } else {
            throw new CostItemNotFoundException(costItemId);
        }
    }
}
