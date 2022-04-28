package nl.bd.garage.repositories;

import nl.bd.garage.models.entities.CostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostItemRepository extends JpaRepository<CostItem, Long> {
    @Query(value = "SELECT * FROM costitems WHERE cost_item_id IN :ids", nativeQuery = true)
    List<CostItem> findCostItemsByIdList(@Param("ids")List<Long> ids);
}
