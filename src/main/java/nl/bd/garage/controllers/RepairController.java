package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.RepairStatus;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetPartsRequest;
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

    // Initial creation of a repair, asks for customerId and inspection date.
    @PostMapping()
    Repair createRepair(@RequestBody RepairRegistrationRequest repairRegistrationRequest) {
        return repairService.createRepair(repairRegistrationRequest);
    }

    // After inspection the found problems will be entered
    @PutMapping("/examined/{repairId}")
    Repair setFoundProblems(@RequestBody String foundProblems, @PathVariable long repairId) {
        return repairService.setFoundProblems(foundProblems, repairId);
    }

    // After the problems have been logged and the customer agrees, a repair will be scheduled with the customer.
    @PutMapping("/setrepairdate/{repairId}")
    Repair setRepairDate(@RequestBody java.sql.Date repairDate, @PathVariable long repairId) {
        return repairService.setRepairDate(repairDate, repairId);
    }

    // After a date has been scheduled, the agreed parts and services will be added to the repair.
    @PutMapping("/setparts/{repairId}")
    Repair setParts(@RequestBody RepairSetPartsRequest repairSetPartsRequest, @PathVariable long repairId) {
        return repairService.setParts(repairSetPartsRequest, repairId);
    }

    // After a repair has been completed, this will be logged in the database.
    @PutMapping("/repaircompleted/{repairId}")
    Repair setComplete(@PathVariable long repairId) {
        return repairService.setComplete(repairId);
    }

    // Àfter the customer has paid, this will be logged in the database.
    @PutMapping("/paymentcompleted/{repairId}")
    Repair setPaymentComplete(@PathVariable long repairId) {
        return repairService.setPaymentComplete(repairId);
    }

    @DeleteMapping("/{repairId}")
    void deleteRepair(@PathVariable long repairId) {
        repairService.deleteRepair(repairId);
    }
}
