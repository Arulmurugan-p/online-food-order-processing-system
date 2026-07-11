package com.foodorder.orderservice.repository;

import com.foodorder.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt >= :since")
    List<Order> findRecentPendingOrders(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    long countPending();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COOKING'")
    long countCooking();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'OUT_FOR_DELIVERY'")
    long countOutForDelivery();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED'")
    long countDelivered();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED' OR o.status = 'REJECTED'")
    long countCancelledOrRejected();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    java.math.BigDecimal sumDeliveredRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    long countTodayOrders();

    @Query("SELECT o FROM Order o WHERE " +
           "(:search IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR CAST(o.id AS string) LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findBySearchTerm(@Param("search") String search, Pageable pageable);
}
