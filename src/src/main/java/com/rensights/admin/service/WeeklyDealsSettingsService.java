package com.rensights.admin.service;

import com.rensights.admin.model.AppSetting;
import com.rensights.admin.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeeklyDealsSettingsService {

    public static final String WEEKLY_DEALS_ENABLED_KEY = "weeklyDeals.enabled";

    private final AppSettingRepository appSettingRepository;

    @Transactional
    public boolean setWeeklyDealsEnabled(boolean enabled) {
        AppSetting setting = appSettingRepository.findById(WEEKLY_DEALS_ENABLED_KEY)
            .orElseGet(() -> AppSetting.builder().settingKey(WEEKLY_DEALS_ENABLED_KEY).build());
        setting.setSettingValue(Boolean.toString(enabled));
        appSettingRepository.save(setting);
        return enabled;
    }

    @Transactional(readOnly = true)
    public boolean isWeeklyDealsEnabled() {
        return appSettingRepository.findById(WEEKLY_DEALS_ENABLED_KEY)
            .map(setting -> Boolean.parseBoolean(setting.getSettingValue()))
            .orElse(true);
    }
}
