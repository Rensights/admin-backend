package com.rensights.admin.service;

import com.rensights.admin.model.AppSetting;
import com.rensights.admin.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleAnalyticsSettingsService {

    public static final String GA_MEASUREMENT_ID_KEY = "googleAnalytics.measurementId";

    private final AppSettingRepository appSettingRepository;

    @Transactional
    public String setMeasurementId(String measurementId) {
        AppSetting setting = appSettingRepository.findById(GA_MEASUREMENT_ID_KEY)
            .orElseGet(() -> AppSetting.builder().settingKey(GA_MEASUREMENT_ID_KEY).build());
        setting.setSettingValue(measurementId == null ? "" : measurementId.trim());
        appSettingRepository.save(setting);
        return setting.getSettingValue();
    }

    @Transactional(readOnly = true)
    public String getMeasurementId() {
        return appSettingRepository.findById(GA_MEASUREMENT_ID_KEY)
            .map(setting -> setting.getSettingValue() == null ? "" : setting.getSettingValue())
            .orElse("");
    }
}
