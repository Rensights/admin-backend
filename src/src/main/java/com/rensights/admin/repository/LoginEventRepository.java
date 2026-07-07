package com.rensights.admin.repository;

import com.rensights.admin.model.LoginEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginEventRepository extends JpaRepository<LoginEvent, UUID> {

    long countByUserId(UUID userId);

    Optional<LoginEvent> findFirstByUserIdOrderByLoggedInAtDesc(UUID userId);

    Page<LoginEvent> findByUserIdOrderByLoggedInAtDesc(UUID userId, Pageable pageable);

    @Query(value = "SELECT COUNT(DISTINCT user_id) FROM login_events WHERE logged_in_at >= :since", nativeQuery = true)
    long countDistinctUsersSince(@Param("since") LocalDateTime since);

    @Query(value = "SELECT CAST(logged_in_at AS DATE) AS day, COUNT(DISTINCT user_id) AS activeUsers "
        + "FROM login_events WHERE logged_in_at >= :since "
        + "GROUP BY CAST(logged_in_at AS DATE) ORDER BY day", nativeQuery = true)
    List<DailyActiveCount> findDailyActiveUserCounts(@Param("since") LocalDateTime since);

    @Query(value = "SELECT user_id AS userId, COUNT(*) AS loginCount, MAX(logged_in_at) AS lastLoginAt "
        + "FROM login_events WHERE user_id IN :userIds GROUP BY user_id", nativeQuery = true)
    List<UserLoginStat> findLoginStatsForUsers(@Param("userIds") Collection<UUID> userIds);

    interface DailyActiveCount {
        java.sql.Date getDay();
        Long getActiveUsers();
    }

    interface UserLoginStat {
        UUID getUserId();
        Long getLoginCount();
        java.sql.Timestamp getLastLoginAt();
    }
}
