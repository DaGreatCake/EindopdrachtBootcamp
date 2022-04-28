package nl.bd.garage.services;

import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.enums.CostType;
import nl.bd.garage.models.exceptions.CostItemNotFoundException;
import nl.bd.garage.models.requests.CostItemRegistrationRequest;
import nl.bd.garage.repositories.CostItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CostItemService {
    @Autowired
    private CostItemRepository costItemRepository;

    public List<CostItem> getAllCostItems() {
        return costItemRepository.findAll();
    }

    public CostItem getCostItemById(long costItemId) {
        return costItemRepository.findById(costItemId).orElseThrow(() -> new CostItemNotFoundException(costItemId));
    }

    public List<CostItem> getCostItemByIdList(List<Long> costItemIdList) {
        return costItemRepository.findCostItemsByIdList(costItemIdList);
    }

    public CostItem createCostItem(CostItemRegistrationRequest costItemRegistrationRequest) {
        CostItem costItem = new CostItem(costItemRegistrationRequest.getName(), costItemRegistrationRequest.getCost(),
                costItemRegistrationRequest.getCostType());
        return costItemRepository.save(costItem);
    }

    public CostItem updateCostItem(CostItem newCostItem, long costItemId) {
        return costItemRepository.findById(costItemId)
                .map(costItem -> {
                    costItem.setName(newCostItem.getName());
                    costItem.setCost(newCostItem.getCost());
                    costItem.setCostType(newCostItem.getCostType());
                    costItem.setStock(newCostItem.getStock());

                    if (costItem.getCostType() == CostType.ACTION) {
                        costItem.setStock(-1);
                    }

                    return costItemRepository.save(costItem);
                })
                .orElseThrow(() -> new CostItemNotFoundException(costItemId));
    }

    public CostItem addStock(int amount, long costItemId) {
        return costItemRepository.findById(costItemId)
                .map(costItem -> {
                    costItem.setStock(costItem.getStock() + amount);
                    return costItemRepository.save(costItem);
                })
                .orElseThrow(() -> new CostItemNotFoundException(costItemId));
    }

    public void deleteCostItem(Long costItemId) {
        if (costItemRepository.findById(costItemId).isPresent()) {
            costItemRepository.deleteById(costItemId);
        } else {
            throw new CostItemNotFoundException(costItemId);
        }
    }
}
