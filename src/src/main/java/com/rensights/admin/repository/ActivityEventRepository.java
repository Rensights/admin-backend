package com.rensights.admin.repository;

import com.rensights.admin.model.ActivityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityEventRepository extends JpaRepository<ActivityEvent, UUID> {

    Page<ActivityEvent> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);

    @Query(value = "SELECT page_path AS pagePath, COUNT(*) AS viewCount "
        + "FROM activity_events WHERE event_type = 'PAGE_VIEW' AND occurred_at >= :since AND page_path IS NOT NULL "
        + "GROUP BY page_path ORDER BY COUNT(*) DESC LIMIT 20", nativeQuery = true)
    List<PageViewCount> findTopPageViews(@Param("since") LocalDateTime since);

    @Query(value = "SELECT event_type AS eventType, COUNT(*) AS eventCount "
        + "FROM activity_events WHERE occurred_at >= :since GROUP BY event_type ORDER BY COUNT(*) DESC", nativeQuery = true)
    List<EventTypeCount> findEventTypeBreakdown(@Param("since") LocalDateTime since);

    interface PageViewCount {
        String getPagePath();
        Long getViewCount();
    }

    interface EventTypeCount {
        String getEventType();
        Long getEventCount();
    }
}
