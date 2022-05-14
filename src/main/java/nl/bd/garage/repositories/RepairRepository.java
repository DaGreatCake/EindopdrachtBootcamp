package nl.bd.garage.repositories;

import nl.bd.garage.models.entities.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {
    // Returns a list of customer ids that have repairs with status CANCELED or COMPLETED
    @Query(value = "SELECT customer_id FROM repairs r WHERE r.completed = 1 OR r.completed = 2"
            , nativeQuery = true)
    List<Long> findCustomerIdsToCall();
}
