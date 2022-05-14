package nl.bd.garage.services;

import nl.bd.garage.models.entities.CostItem;
import nl.bd.garage.models.entities.Customer;
import nl.bd.garage.models.entities.File;
import nl.bd.garage.models.entities.Repair;
import nl.bd.garage.models.enums.CostType;
import nl.bd.garage.models.enums.RepairStatus;
import nl.bd.garage.models.exceptions.*;
import nl.bd.garage.models.requests.RepairRegistrationRequest;
import nl.bd.garage.models.requests.RepairSetPartsRequest;
import nl.bd.garage.repositories.CostItemRepository;
import nl.bd.garage.repositories.CustomerRepository;
import nl.bd.garage.repositories.FileRepository;
import nl.bd.garage.repositories.RepairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/*
* This service will handle all the functionality that is being requested through RepairController and interact with
* the database accordingly.
*/
@Service
public class RepairService {
    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CostItemRepository costItemRepository;

    @Autowired
    private FileRepository fileRepository;

    // Returns all repairs.
    public List<Repair> getAllRepairs() {
        return repairRepository.findAll();
    }

    /*
     * Returns a repair by id.
     *
     * Will throw exception if it doesnt exist.
     */
    public Repair getRepairById(Long repairId) {
        return repairRepository.findById(repairId).orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    // Returns all customers that have to be called. (Their repair was either canceled or completed)
    public List<Customer> getCustomersToCall() {
        List<Long> toCallIds = repairRepository.findCustomerIdsToCall();
        return customerRepository.findCustomersByIdList(toCallIds);
    }

    /*
     * Downloads the papers of the car if they were uploaded.
     *
     * Will throw exception if file or repair doesnt exist.
     */
    public ResponseEntity<Resource> downloadFile(Long repairId) throws java.io.FileNotFoundException {
        if (repairRepository.findById(repairId).orElseThrow(() -> new RepairNotFoundException(repairId))
                .getFile() != null) {
            File dbFile = repairRepository.findById(repairId).get().getFile();

            java.io.File file = new java.io.File(dbFile.getName());
            try {
                OutputStream os = new FileOutputStream(file);
                os.write(dbFile.getContent());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attatchment; filename="+file.getName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            throw new FileNotFoundException(repairId);
        }
    }

    /*
     * Creates a new repair in the database with a given customer id and an examination date.
     *
     * Will throw exception if customerId doesnt exist or no date is given.
     */
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

    /*
     * Uploads a file to the database and links it to a repair. If that repair already has a file, the old file will
     * be deleted.
     *
     * Will throw exception if repair id doesnt exist.
     */
    public Repair uploadFile(MultipartFile multipartFile, Long repairId){
        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (fileRepository.findById(repair.getFile().getFileId()).isPresent()) {
                        fileRepository.deleteById(repair.getFile().getFileId());
                    }

                    try {
                        File file = new File(multipartFile.getBytes(), multipartFile.getOriginalFilename());
                        fileRepository.save(file);
                        repair.setFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId)); 
    }

    /*
     * Adds a string of found problems to the repair for future reference.
     *
     * Will throw exception if repair id doesnt exist or if the string is empty.
     */
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

    /*
     * Sets the status of the repair to CANCELED and customerAgreed to false.
     *
     * Will throw exception if no found problems were entered yet, or if the repair id doesnt exist.
     */
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

    /*
     * Sets the scheduled date for the repair and customerAgreed to true.
     *
     * Will throw exception if no date was given, no found problems were entered yet, the repair was already canceled
     * or if the repair id doesnt exist.
     */
    public Repair setRepairDate(java.sql.Date repairDate, Long repairId) {
        if (repairDate == null) {
            throw new IncorrectSyntaxException("repairDate");
        }

        return repairRepository.findById(repairId)
                .map(repair -> {
                    if (repair.getFoundProblems().equals("")) {
                        throw new PreviousStepUncompletedException("set found problems");
                    } else if (repair.getCustomerAgreed() != null) {
                        if (!repair.getCustomerAgreed()) {
                            throw new CustomerDisagreedException();
                        }
                    }

                    repair.setCustomerAgreed(true);
                    repair.setRepairDate(repairDate);
                    return repairRepository.save(repair);
                })
                .orElseThrow(() -> new RepairNotFoundException(repairId));
    }

    /*
     * Sets the parts used. These parts preexist in a database. A custom amount is also entered.
     *
     * Will throw exception if both the id list of parts used and otherActionsPrice are empty, a given costItem id
     * doesnt exist, a part is out of stock, the customer already disagreed, no repair date was set yet,
     * or the repair id doesnt exist.
     */
    public Repair setParts(RepairSetPartsRequest repairSetPartsRequest, Long repairId) {
        if (repairSetPartsRequest.getPartsUsed() == null && repairSetPartsRequest.getOtherActionsPrice() == 0) {
            throw new IncorrectSyntaxException("partsUsed, otherActionsPrice");
        }

        List<Long> partsList = repairSetPartsRequest.getPartsUsed();
        for (Long l : partsList) {
            if (costItemRepository.findById(l).isEmpty()) {
                throw new CostItemNotFoundException(l);
            } else if (costItemRepository.findById(l).get().getCostType() == CostType.PART &&
                    costItemRepository.findById(l).get().getStock() <= 0) {
                throw new PartOutOfStockException(l);
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

    /*
     * Sets the repair status to COMPLETED.
     *
     * Will throw exception if customer already disagreed, the parts that were used werent entered, or the repair id
     * doesnt exist.
     */
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

    /*
     * Sets the called status to true.
     *
     * Will throw exception if the repair status is still UNCOMPLETED, or the repair id doesnt exist.
     */
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

    /*
     * Returns the receipt of a repair. VAT is calculated and the parts used/actions are being sorted by their type.
     *
     * Will throw exception if customer isnt called yet or the repair id doesnt exist.
     */
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
        if (!repair.getCalled()) {
            throw new PreviousStepUncompletedException("call customer");
        }

        if (repair.getCustomerAgreed()) {
            List<CostItem> costItems = costItemRepository.findCostItemsByIdList(repair.getPartsUsed());

            if (costItems.get(0).getCostItemId() != -1) {
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

            receipt += "\nOther actions : €" + repair.getOtherActionsPrice()
                    + ", VAT: €" + (repair.getOtherActionsPrice() * TAX_PERCENTAGE) + "\n";
            total += repair.getOtherActionsPrice();
        }

        receipt += "\n\n------------------------------------------\n";
        receipt += "Total: €" + total + ", Total VAT: €" + (total * TAX_PERCENTAGE);
        total += total * TAX_PERCENTAGE;
        receipt += "\n\nGrand total: €" + total;

        return receipt;
    }

    /*
     * Sets the payment status to true.
     *
     * Will throw exception if the customer hasnt been called yet or the repair id doesnt exist.
     */
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

    /*
     * Deletes a repair from the database. The linked file will also be deleted from the corresponding table.
     *
     * Will throw exception if the repair id doesnt exist.
     */
    public void deleteRepair(Long repairId) {
        if (repairRepository.findById(repairId).isPresent()) {
            if (fileRepository.findById(repairRepository.findById(repairId).get().getFile().getFileId()).isPresent()) {
                fileRepository.deleteById(repairRepository.findById(repairId).get().getFile().getFileId());
            }
            repairRepository.deleteById(repairId);
        } else {
            throw new RepairNotFoundException(repairId);
        }
    }
}
