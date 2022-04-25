package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairUpdateRequest;
import nl.bd.garage.services.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repairs")
public class RepairController {
    @Autowired
    private RepairService repairService;

    @GetMapping()
    List<Repair> getAllRepairs() {
        return repairService.getAllRepairs();
    }

    @GetMapping("/{repairId}")
    Repair getRepairById(@PathVariable long repairId) {
        return repairService.getRepairById(repairId);
    }

    @PostMapping()
    Repair createRepair(@RequestBody RepairRegistrationRequest repairRegistrationRequest) {
        return repairService.createRepair(repairRegistrationRequest);
    }

    @PutMapping("/{repairId}")
    Repair updateRepair(@RequestBody RepairUpdateRequest repairUpdateRequest, @PathVariable long repairId) {
        return repairService.updateRepair(repairUpdateRequest, repairId);
    }

    @DeleteMapping("/{repairId}")
    void deleteRepair(@PathVariable long repairId) {
        repairService.deleteRepair(repairId);
    }
}
