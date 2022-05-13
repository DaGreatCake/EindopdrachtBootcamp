package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetPartsRequest;
import nl.bd.garage.services.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/repairs")
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

    @Secured({Role.Code.ASSISTANT, Role.Code.ADMIN})
    @GetMapping("/completed")
    List<Customer> getCustomersToCall() {
        return repairService.getCustomersToCall();
    }

    @GetMapping(path = "/papers/{repairId}")
    ResponseEntity<Resource> downloadFile(@PathVariable long repairId) throws FileNotFoundException {
        return repairService.downloadFile(repairId);
    }

    // Initial creation of a repair, asks for customerId and inspection date.
    @Secured({Role.Code.ASSISTANT, Role.Code.ADMIN})
    @PostMapping()
    Repair createRepair(@RequestBody RepairRegistrationRequest repairRegistrationRequest) {
        return repairService.createRepair(repairRegistrationRequest);
    }

    // After the creation of the repair, an optional file can be uploaded of the cars papers.
    @Secured({Role.Code.ASSISTANT, Role.Code.ADMIN})
    @PutMapping(path = "/papers/{repairId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Repair uploadFile(@RequestPart(required = false) MultipartFile multipartFile, @PathVariable long repairId) {
        return repairService.uploadFile(multipartFile, repairId);
    }

    // After inspection the found problems will be entered
    @Secured({Role.Code.MECHANIC, Role.Code.ADMIN})
    @PutMapping("/examined/{repairId}")
    Repair setFoundProblems(@RequestBody String foundProblems, @PathVariable long repairId) {
        return repairService.setFoundProblems(foundProblems, repairId);
    }

    // After the problems have been logged and the customer disagrees, the repair will be canceled.
    @Secured({Role.Code.MECHANIC, Role.Code.ADMIN})
    @PutMapping("/cancel/{repairId}")
    Repair setCanceled(@PathVariable long repairId) {
        return repairService.setCanceled(repairId);
    }

    // After the problems have been logged and the customer agrees, a repair will be scheduled with the customer.
    @Secured({Role.Code.MECHANIC, Role.Code.ADMIN})
    @PutMapping("/setrepairdate/{repairId}")
    Repair setRepairDate(@RequestBody java.sql.Date repairDate, @PathVariable long repairId) {
        return repairService.setRepairDate(repairDate, repairId);
    }

    // After a date has been scheduled, the agreed parts and services will be added to the repair.
    @Secured({Role.Code.MECHANIC, Role.Code.ADMIN})
    @PutMapping("/setparts/{repairId}")
    Repair setParts(@RequestBody RepairSetPartsRequest repairSetPartsRequest, @PathVariable long repairId) {
        return repairService.setParts(repairSetPartsRequest, repairId);
    }

    // After a repair has been completed, this will be logged in the database.
    @Secured({Role.Code.MECHANIC, Role.Code.ADMIN})
    @PutMapping("/setrepaircompleted/{repairId}")
    Repair setComplete(@PathVariable long repairId) {
        return repairService.setComplete(repairId);
    }

    // After a customer has been called to retrieve their car, this will be logged so they wont be called again.
    @Secured({Role.Code.ASSISTANT, Role.Code.ADMIN})
    @PutMapping("/setcalled/{repairId}")
    Repair setCalled(@PathVariable long repairId) {
        return repairService.setCalled(repairId);
    }

    // When the customer comes to retrieve the car, a receipt can be generated.
    @Secured({Role.Code.CASHIER, Role.Code.ADMIN})
    @GetMapping("/receipt/{repairId}")
    String getReceipt(@PathVariable long repairId) {
        return repairService.getReceipt(repairId);
    }

    // Àfter the customer has paid, this will be logged in the database.
    @Secured({Role.Code.CASHIER, Role.Code.ADMIN})
    @PutMapping("/setpaymentcompleted/{repairId}")
    Repair setPaymentComplete(@PathVariable long repairId) {
        return repairService.setPaymentComplete(repairId);
    }

    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{repairId}")
    void deleteRepair(@PathVariable long repairId) {
        repairService.deleteRepair(repairId);
    }
}
