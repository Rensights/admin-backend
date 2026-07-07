package com.rensights.admin.service;

import com.rensights.admin.dto.ActivityTimelineItemDTO;
import com.rensights.admin.dto.CustomerAnalyticsSummaryDTO;
import com.rensights.admin.dto.CustomerLoginStatDTO;
import com.rensights.admin.dto.DailyActiveUsersPointDTO;
import com.rensights.admin.dto.EventTypeStatDTO;
import com.rensights.admin.dto.LoginEventDTO;
import com.rensights.admin.dto.PageViewStatDTO;
import com.rensights.admin.dto.UserLoginSummaryDTO;
import com.rensights.admin.model.ActivityEvent;
import com.rensights.admin.model.LoginEvent;
import com.rensights.admin.model.User;
import com.rensights.admin.repository.ActivityEventRepository;
import com.rensights.admin.repository.LoginEventRepository;
import com.rensights.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerAnalyticsService {

    private final UserRepository userRepository;
    private final LoginEventRepository loginEventRepository;
    private final ActivityEventRepository activityEventRepository;

    @Transactional(readOnly = true)
    public CustomerAnalyticsSummaryDTO getSummary() {
        LocalDateTime now = LocalDateTime.now();
        long dau = loginEventRepository.countDistinctUsersSince(now.minusHours(24));
        long mau = loginEventRepository.countDistinctUsersSince(now.minusDays(30));
        long totalUsers = userRepository.count();
        // Strict 5-minute presence window - no grace period for a missed heartbeat.
        long activeNow = userRepository.countByLastSeenAtAfter(now.minusMinutes(5));
        return CustomerAnalyticsSummaryDTO.builder()
            .dailyActiveUsers(dau)
            .monthlyActiveUsers(mau)
            .totalUsers(totalUsers)
            .activeNow(activeNow)
            .build();
    }

    @Transactional(readOnly = true)
    public List<PageViewStatDTO> getPageViewStats(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityEventRepository.findTopPageViews(since).stream()
            .map(row -> new PageViewStatDTO(row.getPagePath(), row.getViewCount()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventTypeStatDTO> getEventTypeBreakdown(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return activityEventRepository.findEventTypeBreakdown(since).stream()
            .map(row -> new EventTypeStatDTO(row.getEventType(), row.getEventCount()))
            .collect(Collectors.toList());
    }

    /**
     * Combined login + activity-event feed for one customer, newest first.
     * Pulls a bounded recent window from each source and merges in memory -
     * simple and correct for "recent activity" browsing, not built for deep
     * pagination over a user's entire lifetime history.
     */
    @Transactional(readOnly = true)
    public Page<ActivityTimelineItemDTO> getUserActivityTimeline(UUID userId, int page, int size) {
        int fetchWindow = (page + 1) * size + 50;
        List<LoginEvent> logins = loginEventRepository
            .findByUserIdOrderByLoggedInAtDesc(userId, PageRequest.of(0, fetchWindow))
            .getContent();
        List<ActivityEvent> events = activityEventRepository
            .findByUserIdOrderByOccurredAtDesc(userId, PageRequest.of(0, fetchWindow))
            .getContent();

        List<ActivityTimelineItemDTO> merged = new ArrayList<>();
        for (LoginEvent login : logins) {
            merged.add(ActivityTimelineItemDTO.builder()
                .eventType("LOGIN")
                .occurredAt(login.getLoggedInAt())
                .metadata(login.getIpAddress())
                .build());
        }
        for (ActivityEvent event : events) {
            merged.add(ActivityTimelineItemDTO.builder()
                .eventType(event.getEventType())
                .pagePath(event.getPagePath())
                .metadata(event.getMetadata())
                .occurredAt(event.getOccurredAt())
                .build());
        }
        merged.sort(Comparator.comparing(ActivityTimelineItemDTO::getOccurredAt).reversed());

        int fromIndex = Math.min(page * size, merged.size());
        int toIndex = Math.min(fromIndex + size, merged.size());
        List<ActivityTimelineItemDTO> pageContent = merged.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), merged.size());
    }

    @Transactional(readOnly = true)
    public List<DailyActiveUsersPointDTO> getDailyTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days).toLocalDate().atStartOfDay();
        return loginEventRepository.findDailyActiveUserCounts(since).stream()
            .map(row -> new DailyActiveUsersPointDTO(row.getDay().toLocalDate().toString(), row.getActiveUsers()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CustomerLoginStatDTO> getCustomerLoginStats(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> usersPage = userRepository.findAll(pageable);

        List<UUID> userIds = usersPage.getContent().stream().map(User::getId).collect(Collectors.toList());
        Map<UUID, LoginEventRepository.UserLoginStat> statsByUser = userIds.isEmpty()
            ? Map.of()
            : loginEventRepository.findLoginStatsForUsers(userIds).stream()
                .collect(Collectors.toMap(LoginEventRepository.UserLoginStat::getUserId, s -> s));

        List<CustomerLoginStatDTO> dtos = usersPage.getContent().stream()
            .map(user -> {
                LoginEventRepository.UserLoginStat stat = statsByUser.get(user.getId());
                return CustomerLoginStatDTO.builder()
                    .userId(user.getId().toString())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .loginCount(stat != null ? stat.getLoginCount() : 0L)
                    .lastLoginAt(stat != null && stat.getLastLoginAt() != null
                        ? stat.getLastLoginAt().toLocalDateTime() : null)
                    .build();
            })
            .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, usersPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserLoginSummaryDTO getUserLoginSummary(UUID userId) {
        long count = loginEventRepository.countByUserId(userId);
        LocalDateTime lastLogin = loginEventRepository.findFirstByUserIdOrderByLoggedInAtDesc(userId)
            .map(LoginEvent::getLoggedInAt)
            .orElse(null);
        return new UserLoginSummaryDTO(count, lastLogin);
    }

    @Transactional(readOnly = true)
    public Page<LoginEventDTO> getUserLoginHistory(UUID userId, int page, int size) {
        Page<LoginEvent> events = loginEventRepository.findByUserIdOrderByLoggedInAtDesc(
            userId, PageRequest.of(page, size));
        return events.map(e -> new LoginEventDTO(e.getLoggedInAt(), e.getIpAddress()));
    }
}
