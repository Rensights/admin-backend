package com.rensights.admin.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mirrors com.rensights.model.LoginEvent in app-backend (same table, written
 * there on every successful login). Every column has an explicit name - do
 * not rely on implicit naming strategy here (see the AppSetting
 * settingKey/setting_key incident for why).
 */
@Entity
@Table(name = "login_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "logged_in_at", nullable = false)
    private LocalDateTime loggedInAt;

    @Column(name = "ip_address")
    private String ipAddress;
}
