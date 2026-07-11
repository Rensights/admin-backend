package com.rensights.admin.repository;

import com.rensights.admin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByUserTier(User.UserTier userTier);
    long countByIsActive(boolean isActive);
    long countByEmailVerified(boolean emailVerified);

    // Strict presence window - no grace period for a missed heartbeat.
    long countByLastSeenAtAfter(LocalDateTime since);

    // Baseline for the cumulative customer-growth curve: everyone who signed up
    // before the charted window, so the first bucket's running total is the true all-time total.
    long countByCreatedAtBefore(LocalDateTime before);

    @Query(value = "SELECT TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') AS month, "
        + "COUNT(*) AS newCustomers "
        + "FROM users WHERE created_at >= :since "
        + "GROUP BY DATE_TRUNC('month', created_at) "
        + "ORDER BY DATE_TRUNC('month', created_at)", nativeQuery = true)
    List<MonthlyNewCount> findMonthlyNewUserCounts(@Param("since") LocalDateTime since);

    interface MonthlyNewCount {
        String getMonth();
        Long getNewCustomers();
    }
}

