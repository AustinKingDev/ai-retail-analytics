package com.ai.agent.ai_agent.repository;

import com.ai.agent.ai_agent.entity.ItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, String> {

    @Query("SELECT i FROM ItemEntity i WHERE i.onlineAvailable = true AND i.storeAvailable = false ORDER BY i.storePrice DESC")
    List<ItemEntity> findOnlineOnlyItemsByStorePriceDesc(Pageable pageable);

    @Query("SELECT i FROM ItemEntity i WHERE i.storeAvailable = true AND i.onlineAvailable = false ORDER BY i.storePrice DESC")
    List<ItemEntity> findStoreOnlyItemsByStorePriceDesc(Pageable pageable);

    @Query("SELECT i FROM ItemEntity i WHERE i.storeAvailable = true AND i.onlineAvailable = true ORDER BY i.storePrice DESC")
    List<ItemEntity> findOnlineAndStoreItemsByStorePriceDesc(Pageable pageable);

    @Query("SELECT i FROM ItemEntity i WHERE i.quantityInStock < :maxStock AND i.unitsSold > :minUnitsSold ORDER BY i.unitsSold DESC")
    List<ItemEntity> findItemsWithLowStockAndHighSales(@Param("maxStock") int maxStock, @Param("minUnitsSold") int minUnitsSold);

    @Query("SELECT i FROM ItemEntity i WHERE i.unitsSold <= :maxUnitsSold AND i.averageRating <= :maxAverageRating ORDER BY i.unitsSold ASC, i.averageRating ASC")
    List<ItemEntity> findUnderperformingItems(@Param("maxUnitsSold") int maxUnitsSold, @Param("maxAverageRating") double maxAverageRating);

    @Query("SELECT i FROM ItemEntity i WHERE i.unitsSold >= :minUnitsSold AND i.averageRating >= :minAverageRating ORDER BY i.unitsSold DESC, i.averageRating DESC")
    List<ItemEntity> findTopPerformingItems(
            @Param("minUnitsSold") int minUnitsSold,
            @Param("minAverageRating") double minAverageRating,
            Pageable pageable
    );

    List<ItemEntity> findByCategoryIgnoreCase(String category);


}
