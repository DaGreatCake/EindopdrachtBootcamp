package nl.bd.garage.services;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairUpdateRequest;
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

    public Repair getRepairById(long id) {
        return repairRepository.getById(id);
    }

    public Repair createRepair(RepairRegistrationRequest repairRegistrationRequest) {
        if (customerRepository.findById(repairRegistrationRequest.getCustomerId()).isPresent()) {
            Repair newRepair = new Repair(customerRepository.findById(repairRegistrationRequest.getCustomerId()).get(),
                    repairRegistrationRequest.getDate());
            return repairRepository.save(newRepair);
        }

        return null;
    }

    public Repair updateRepair(RepairUpdateRequest repairUpdateRequest, Long repairId) {
        return repairRepository.findById(repairId)
                .map(repair -> {
                    if(repairUpdateRequest.getExaminationDate() != null) {
                        repair.setExaminationDate(repairUpdateRequest.getExaminationDate());
                    }
                    if(repairUpdateRequest.getFoundProblems() != null) {
                        repair.setFoundProblems(repairUpdateRequest.getFoundProblems());
                    }
                    if(repairUpdateRequest.getRepairDate() != null) {
                        repair.setRepairDate(repairUpdateRequest.getRepairDate());
                    }
                    if(repairUpdateRequest.getCustomerAgreed() != null) {
                        repair.setCustomerAgreed(repairUpdateRequest.getCustomerAgreed());
                    }
                    if(repairUpdateRequest.getPartsUsed() != null) {
                        repair.setPartsUsed(repairUpdateRequest.getPartsUsed());
                    }
                    if(repairUpdateRequest.getOtherActionsPrice() != 0.0) {
                        repair.setOtherActionsPrice(repairUpdateRequest.getOtherActionsPrice());
                    }
                    if(repairUpdateRequest.getCompleted() != null) {
                        repair.setCompleted(repairUpdateRequest.getCompleted());
                    }
                    if(repairUpdateRequest.getPaid() != null) {
                        repair.setPaid(repairUpdateRequest.getPaid());
                    }

                    return repairRepository.save(repair);
                })
                .orElseGet(() -> {
                    return null;
                });
    }

    public void deleteRepair(Long id) {
        repairRepository.deleteById(id);
    }
}
