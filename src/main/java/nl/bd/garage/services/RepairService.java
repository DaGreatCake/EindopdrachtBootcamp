package nl.bd.garage.services;

import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.CostType;
import nl.bd.garage.models.enums.RepairStatus;
import nl.bd.garage.models.exceptions.*;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetPartsRequest;
import nl.bd.garage.repositories.CostItemRepository;
import nl.bd.garage.repositories.CustomerRepository;
import nl.bd.garage.repositories.RepairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairService {
    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CostItemRepository costItemRepository;

    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }

    public Repair getRepairById(long repairId) {
        return repairRepository.findById(repairId).orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public List<Customer> getCustomersToCall() {
        List<Long> toCallIds = repairRepository.findCustomerIdsToCall();
        return customerRepository.findCustomersByIdList(toCallIds);
    }

    public Repair createRepair(RepairRegistrationRequest repairRegistrationRequest) {
        if (customerRepository.findById(repairRegistrationRequest.getCustomerId()).isPresent()) {
            Repair newRepair = new Repair(customerRepository.findById(repairRegistrationRequest.getCustomerId()).get(),
                    repairRegistrationRequest.getExaminationDate());

            if (newRepair.getExaminationDate() == null) {
                throw new IncorrectSyntaxException("examinationDate");
            }

            return repairRepository.save(newRepair);
        } else {
            throw new CustomerNotFoundException(repairRegistrationRequest.getCustomerId());
        }
    }

    public Repair setFoundProblems(String foundProblems, Long repairId) {
        if (foundProblems.equals("")) {
            throw new IncorrectSyntaxException("foundProblems");
        }

        return repairRepository.findById(repairId)
                .map(repair -> {
                    repair.setFoundProblems(foundProblems);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setCanceled(Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (repair.getFoundProblems().equals("")) {
                        throw new PreviousStepUncompletedException("set found problems");
                    }

                    repair.setCustomerAgreed(false);
                    repair.setCompleted(RepairStatus.CANCELED);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setRepairDate(java.sql.Date repairDate, Long repairId) {
        if (repairDate == null) {
            throw new IncorrectSyntaxException("repairDate");
        }

        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (repair.getFoundProblems().equals("")) {
                        throw new PreviousStepUncompletedException("set found problems");
                    } else if (!repair.getCustomerAgreed()) {
                        throw new CustomerDisagreedException();
                    }

                    repair.setCustomerAgreed(true);
                    repair.setRepairDate(repairDate);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setParts(RepairSetPartsRequest repairSetPartsRequest, Long repairId) {
        if (repairSetPartsRequest.getPartsUsed() == null && repairSetPartsRequest.getOtherActionsPrice() == 0) {
            throw new IncorrectSyntaxException("partsUsed, otherActionsPrice");
        }

        List<Long> partsList = repairSetPartsRequest.getPartsUsed();
        for (Long l : partsList) {
            if (costItemRepository.findById(l).isEmpty()) {
                throw new CostItemNotFoundException(l);
            }
        }

        if (partsList.isEmpty()) {
            partsList.add((long) -1);
        }

        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (!repair.getCustomerAgreed()) {
                        throw new CustomerDisagreedException();
                    } else if (repair.getCustomerAgreed() == null) {
                        throw new PreviousStepUncompletedException("set repair date (if customer agrees)");
                    }

                    repair.setPartsUsed(partsList);
                    repair.setOtherActionsPrice(repairSetPartsRequest.getOtherActionsPrice());
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setComplete(Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (!repair.getCustomerAgreed()) {
                        throw new CustomerDisagreedException();
                    } else if (repair.getPartsUsed() == null) {
                        throw new PreviousStepUncompletedException("set parts used");
                    }

                    repair.setCompleted(RepairStatus.COMPLETED);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setCalled(Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (repair.getCompleted() == RepairStatus.UNCOMPLETED) {
                        throw new PreviousStepUncompletedException("complete or cancel repair");
                    }

                    repair.setCalled(true);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public String getReceipt(Long repairId) {
        double TAX_PERCENTAGE = 0.21;
        double examinationPrice = 45;
        double total = examinationPrice;
        Repair repair;
        String receipt = "Examination: €" + examinationPrice + ", VAT: €" + (examinationPrice * TAX_PERCENTAGE);

        if (repairRepository.findById(repairId).isPresent()) {
            repair = repairRepository.findById(repairId).get();
        } else {
            throw new RepairNotFoundException(repairId);
        }

        if (repair.getCustomerAgreed()) {
            List<CostItem> costItems = costItemRepository.findCostItemsByIdList(repair.getPartsUsed());
            receipt += "\n\nParts used: \n";

            for (CostItem costItem : costItems) {
                if (costItem.getCostType() == CostType.PART) {
                    receipt += costItem.getName() + " : €" + costItem.getCost()
                            + ", VAT: €" + (costItem.getCost() * TAX_PERCENTAGE) + "\n";
                    total += costItem.getCost();
                }
            }

            receipt += "\nActions done: \n";
            for (CostItem costItem : costItems) {
                if (costItem.getCostType() == CostType.ACTION) {
                    receipt += costItem.getName() + " : €" + costItem.getCost()
                            + ", VAT: €" + (costItem.getCost() * TAX_PERCENTAGE) + "\n";
                    total += costItem.getCost();
                }
            }
        }

        receipt += "\n\n------------------------------------------\n";
        receipt += "Total: €" + total + ", Total VAT: €" + (total * TAX_PERCENTAGE);
        total += total * TAX_PERCENTAGE;
        receipt += "\n\nGrand total: €" + total;

        return receipt;
    }

    public Repair setPaymentComplete(Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (!repair.getCalled()) {
                        throw new PreviousStepUncompletedException("call customer");
                    }

                    repair.setPaid(true);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public void deleteRepair(Long repairId) {
        if (repairRepository.findById(repairId).isPresent()) {
            repairRepository.deleteById(repairId);
        } else {
            throw new RepairNotFoundException(repairId);
        }
    }
}
