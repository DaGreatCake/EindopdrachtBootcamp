package nl.bd.garage.services;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.RepairStatus;
import nl.bd.garage.models.exceptions.CustomerNotFoundException;
import nl.bd.garage.models.exceptions.RepairNotFoundException;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetPartsRequest;
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
                    repairRegistrationRequest.getDate());
            return repairRepository.save(newRepair);
        } else {
            throw new CustomerNotFoundException(repairRegistrationRequest.getCustomerId());
        }
    }

    public Repair setFoundProblems(String foundProblems, Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    repair.setFoundProblems(foundProblems);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setRepairDate(java.sql.Date repairDate, Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    repair.setCustomerAgreed(true);
                    repair.setRepairDate(repairDate);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setParts(RepairSetPartsRequest repairSetPartsRequest, Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    repair.setPartsUsed(repairSetPartsRequest.getPartsUsed());
                    repair.setOtherActionsPrice(repairSetPartsRequest.getOtherActionsPrice());
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setComplete(Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    repair.setCompleted(RepairStatus.COMPLETED);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    public Repair setPaymentComplete(Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
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
