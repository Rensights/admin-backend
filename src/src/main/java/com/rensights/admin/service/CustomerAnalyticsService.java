package com.rensights.admin.service;

import com.rensights.admin.dto.ActivityTimelineItemDTO;
import com.rensights.admin.dto.CustomerAnalyticsSummaryDTO;
import com.rensights.admin.dto.CustomerGrowthPointDTO;
import com.rensights.admin.dto.CustomerLoginStatDTO;
import com.rensights.admin.dto.DailyActiveUsersPointDTO;
import com.rensights.admin.dto.EventTypeStatDTO;
import com.rensights.admin.dto.LoginEventDTO;
import com.rensights.admin.dto.MonthlyActiveUsersPointDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

    /**
     * Distinct active users per calendar month over the last {@code months} months,
     * gap-filled with zeros so the chart is continuous.
     */
    @Transactional(readOnly = true)
    public List<MonthlyActiveUsersPointDTO> getMonthlyActiveTrend(int months) {
        LocalDateTime since = firstOfMonthWindow(months);
        Map<String, Long> byMonth = loginEventRepository.findMonthlyActiveUserCounts(since).stream()
            .collect(Collectors.toMap(
                LoginEventRepository.MonthlyActiveCount::getMonth,
                LoginEventRepository.MonthlyActiveCount::getActiveUsers));

        List<MonthlyActiveUsersPointDTO> result = new ArrayList<>();
        for (YearMonth ym = YearMonth.from(since); !ym.isAfter(YearMonth.now()); ym = ym.plusMonths(1)) {
            String key = ym.toString(); // "YYYY-MM"
            result.add(new MonthlyActiveUsersPointDTO(key, byMonth.getOrDefault(key, 0L)));
        }
        return result;
    }

    /**
     * New customers per calendar month plus the running all-time total (cumulative
     * seeded with everyone who signed up before the window). Gap-filled with zeros.
     */
    @Transactional(readOnly = true)
    public List<CustomerGrowthPointDTO> getCustomerGrowthTrend(int months) {
        LocalDateTime since = firstOfMonthWindow(months);
        Map<String, Long> newByMonth = userRepository.findMonthlyNewUserCounts(since).stream()
            .collect(Collectors.toMap(
                UserRepository.MonthlyNewCount::getMonth,
                UserRepository.MonthlyNewCount::getNewCustomers));

        long cumulative = userRepository.countByCreatedAtBefore(since);
        List<CustomerGrowthPointDTO> result = new ArrayList<>();
        for (YearMonth ym = YearMonth.from(since); !ym.isAfter(YearMonth.now()); ym = ym.plusMonths(1)) {
            long added = newByMonth.getOrDefault(ym.toString(), 0L);
            cumulative += added;
            result.add(new CustomerGrowthPointDTO(ym.toString(), added, cumulative));
        }
        return result;
    }

    /** Full per-customer login stats as CSV (all users, unpaginated) for aggregate analysis. */
    @Transactional(readOnly = true)
    public String buildCustomerLoginStatsCsv() {
        List<User> users = userRepository.findAll(Sort.by("createdAt").descending());
        List<UUID> ids = users.stream().map(User::getId).collect(Collectors.toList());
        Map<UUID, LoginEventRepository.UserLoginStat> statsByUser = ids.isEmpty()
            ? Map.of()
            : loginEventRepository.findLoginStatsForUsers(ids).stream()
                .collect(Collectors.toMap(LoginEventRepository.UserLoginStat::getUserId, s -> s));

        StringBuilder sb = new StringBuilder();
        sb.append("userId,email,firstName,lastName,tier,createdAt,loginCount,lastLoginAt\n");
        for (User u : users) {
            LoginEventRepository.UserLoginStat stat = statsByUser.get(u.getId());
            long loginCount = stat != null ? stat.getLoginCount() : 0L;
            String lastLogin = stat != null && stat.getLastLoginAt() != null
                ? stat.getLastLoginAt().toLocalDateTime().toString() : "";
            sb.append(csv(u.getId().toString())).append(',')
              .append(csv(u.getEmail())).append(',')
              .append(csv(u.getFirstName())).append(',')
              .append(csv(u.getLastName())).append(',')
              .append(csv(u.getUserTier() != null ? u.getUserTier().name() : "")).append(',')
              .append(csv(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "")).append(',')
              .append(loginCount).append(',')
              .append(csv(lastLogin)).append('\n');
        }
        return sb.toString();
    }

    /**
     * One flat CSV of EVERYTHING: every login and every activity event across all
     * customers, one row per event, with the owning customer's details on each row
     * so it's clear who it belongs to. Rows are grouped by customer email then time.
     */
    @Transactional(readOnly = true)
    public String buildFullExportCsv() {
        Map<UUID, User> usersById = userRepository.findAll().stream()
            .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<String[]> rows = new ArrayList<>();

        for (LoginEvent e : loginEventRepository.findAll()) {
            User u = usersById.get(e.getUserId());
            rows.add(eventRow(u, e.getUserId(), "LOGIN", "LOGIN",
                e.getLoggedInAt(), e.getIpAddress(), "", ""));
        }
        for (ActivityEvent e : activityEventRepository.findAll()) {
            User u = usersById.get(e.getUserId());
            rows.add(eventRow(u, e.getUserId(), "ACTIVITY",
                e.getEventType() != null ? e.getEventType() : "",
                e.getOccurredAt(), "", e.getPagePath(), e.getMetadata()));
        }

        // Group by customer (email), then chronological within each customer.
        rows.sort(Comparator
            .comparing((String[] r) -> r[1])
            .thenComparing(r -> r[8]));

        StringBuilder sb = new StringBuilder();
        sb.append("userId,email,firstName,lastName,tier,userCreatedAt,")
          .append("eventCategory,eventType,occurredAt,ipAddress,pagePath,metadata\n");
        for (String[] r : rows) {
            for (int i = 0; i < r.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(csv(r[i]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String[] eventRow(User u, UUID userId, String category, String type,
                                     LocalDateTime occurredAt, String ip, String pagePath, String metadata) {
        return new String[] {
            userId != null ? userId.toString() : "",
            u != null && u.getEmail() != null ? u.getEmail() : "",
            u != null && u.getFirstName() != null ? u.getFirstName() : "",
            u != null && u.getLastName() != null ? u.getLastName() : "",
            u != null && u.getUserTier() != null ? u.getUserTier().name() : "",
            u != null && u.getCreatedAt() != null ? u.getCreatedAt().toString() : "",
            category,
            type,
            occurredAt != null ? occurredAt.toString() : "",
            ip != null ? ip : "",
            pagePath != null ? pagePath : "",
            metadata != null ? metadata : "",
        };
    }

    private static LocalDateTime firstOfMonthWindow(int months) {
        int span = Math.max(1, months);
        return LocalDate.now().withDayOfMonth(1).minusMonths(span - 1L).atStartOfDay();
    }

    /** Minimal RFC-4180 CSV field escaping. */
    private static String csv(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
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
