package nl.bd.garage.controllers;

import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.Role;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetFoundProblemsRequest;
import nl.bd.garage.models.requests.RepairSetPartsRequest;
import nl.bd.garage.models.requests.RepairSetRepairDateRequest;
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

/*
 * Handles http requests on endpoints concerning repairs.
 */
@RestController
@RequestMapping("/api/repairs")
public class RepairController {
    @Autowired
    private RepairService repairService;

    // Returns all repairs.
    @GetMapping()
    List<Repair> getAllRepairs() {
        return repairService.getAllRepairs();
    }

    /*
     * Returns a repair by repair id.
     *
     * Provide repair id in the url.
     */
    @GetMapping("/{repairId}")
    Repair getRepairById(@PathVariable long repairId) {
        return repairService.getRepairById(repairId);
    }

    // Returns a list of customers to call.
    @Secured(Role.Code.ASSISTANT)
    @GetMapping("/completed")
    List<Customer> getCustomersToCall() {
        return repairService.getCustomersToCall();
    }

    /*
     * Downloads a file with the papers of a car.
     *
     * Provide repair id in the url.
     */
    @GetMapping(path = "/papers/{repairId}")
    ResponseEntity<Resource> downloadFile(@PathVariable long repairId) throws FileNotFoundException {
        return repairService.downloadFile(repairId);
    }

    /*
     * Creates a new repair in the database.
     *
     * Provide customerId:long, examinationDate:Date in the body.
     */
    @Secured(Role.Code.ASSISTANT)
    @PostMapping()
    Repair createRepair(@RequestBody RepairRegistrationRequest repairRegistrationRequest) {
        return repairService.createRepair(repairRegistrationRequest);
    }

    /*
     * After the creation of the repair, an optional file can be uploaded of the cars papers.
     *
     * Provide repair id in the url. Provide file:File in the body.
     */
    @Secured(Role.Code.ASSISTANT)
    @PutMapping(path = "/papers/{repairId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Repair uploadFile(@RequestPart(required = false) MultipartFile file, @PathVariable long repairId) {
        return repairService.uploadFile(file, repairId);
    }

    /*
     * After inspection the found problems will be entered
     *
     * Provide repair id in the url. Provide foundProblems:String in the body.
     */
    @Secured(Role.Code.MECHANIC)
    @PutMapping("/examined/{repairId}")
    Repair setFoundProblems(@RequestBody RepairSetFoundProblemsRequest foundProblems, @PathVariable long repairId) {
        return repairService.setFoundProblems(foundProblems.getFoundProblems(), repairId);
    }

    /*
     * After the problems have been logged and the customer disagrees, the repair will be canceled.
     *
     * Provide repair id in the url.
     */
    @Secured(Role.Code.MECHANIC)
    @PutMapping("/cancel/{repairId}")
    Repair setCanceled(@PathVariable long repairId) {
        return repairService.setCanceled(repairId);
    }

    /*
     * After the problems have been logged and the customer agrees, a repair will be scheduled with the customer.
     *
     * Provide repair id in the url. Provide repairDate:Date in the body.
     */
    @Secured(Role.Code.MECHANIC)
    @PutMapping("/setrepairdate/{repairId}")
    Repair setRepairDate(@RequestBody RepairSetRepairDateRequest repairDate, @PathVariable long repairId) {
        return repairService.setRepairDate(repairDate.getRepairDate(), repairId);
    }

    /*
     * After a date has been scheduled, the agreed parts and services will be added to the repair.
     *
     * Provide repair id in the url. Provide partsUsed:List<long> and otherActionsPrice:double in the body.
     */
    @Secured(Role.Code.MECHANIC)
    @PutMapping("/setparts/{repairId}")
    Repair setParts(@RequestBody RepairSetPartsRequest repairSetPartsRequest, @PathVariable long repairId) {
        return repairService.setParts(repairSetPartsRequest, repairId);
    }

    /*
     * After a repair has been completed, this will be logged in the database.
     *
     * Provide repair id in the url.
     */
    @Secured(Role.Code.MECHANIC)
    @PutMapping("/setrepaircompleted/{repairId}")
    Repair setComplete(@PathVariable long repairId) {
        return repairService.setComplete(repairId);
    }

    /*
     * After a customer has been called to retrieve their car, this will be logged so they wont be called again.
     *
     * Provide repair id in the url.
     */
    @Secured(Role.Code.ASSISTANT)
    @PutMapping("/setcalled/{repairId}")
    Repair setCalled(@PathVariable long repairId) {
        return repairService.setCalled(repairId);
    }

    /*
     * When the customer comes to retrieve the car, a receipt can be generated.
     *
     * Provide repair id in the url.
     */
    @Secured(Role.Code.CASHIER)
    @GetMapping("/receipt/{repairId}")
    String getReceipt(@PathVariable long repairId) {
        return repairService.getReceipt(repairId);
    }

    /*
     * Àfter the customer has paid, this will be logged in the database.
     *
     * Provide repair id in the url.
     */
    @Secured(Role.Code.CASHIER)
    @PutMapping("/setpaymentcompleted/{repairId}")
    Repair setPaymentComplete(@PathVariable long repairId) {
        return repairService.setPaymentComplete(repairId);
    }

    /*
     * Deletes a repair from the database.
     *
     * Provide repair id in the url.
     */
    @Secured(Role.Code.ADMIN)
    @DeleteMapping("/{repairId}")
    void deleteRepair(@PathVariable long repairId) {
        repairService.deleteRepair(repairId);
    }
}
