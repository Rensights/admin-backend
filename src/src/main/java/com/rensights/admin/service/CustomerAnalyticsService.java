package com.rensights.admin.service;

import com.rensights.admin.dto.CustomerAnalyticsSummaryDTO;
import com.rensights.admin.dto.CustomerLoginStatDTO;
import com.rensights.admin.dto.DailyActiveUsersPointDTO;
import com.rensights.admin.dto.LoginEventDTO;
import com.rensights.admin.dto.UserLoginSummaryDTO;
import com.rensights.admin.model.LoginEvent;
import com.rensights.admin.model.User;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerAnalyticsService {

    private final UserRepository userRepository;
    private final LoginEventRepository loginEventRepository;

    @Transactional(readOnly = true)
    public CustomerAnalyticsSummaryDTO getSummary() {
        LocalDateTime now = LocalDateTime.now();
        long dau = loginEventRepository.countDistinctUsersSince(now.minusHours(24));
        long mau = loginEventRepository.countDistinctUsersSince(now.minusDays(30));
        long totalUsers = userRepository.count();
        return CustomerAnalyticsSummaryDTO.builder()
            .dailyActiveUsers(dau)
            .monthlyActiveUsers(mau)
            .totalUsers(totalUsers)
            .build();
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
