package com.rensights.admin.config;

import com.rensights.admin.model.ReportSection;
import com.rensights.admin.repository.LanguageRepository;
import com.rensights.admin.repository.ReportSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportSectionSeeder implements ApplicationRunner {

    private final ReportSectionRepository reportSectionRepository;
    private final LanguageRepository languageRepository;

    private record DefaultSection(
        String key,
        String title,
        String navTitle,
        String description,
        ReportSection.AccessTier accessTier,
        int displayOrder
    ) {}

    private static final List<DefaultSection> DEFAULTS = List.of(
        new DefaultSection(
            "market-outlook",
            "Dubai Real Estate Market Outlook",
            "Real Estate Market Outlook",
            "A high-level strategic overview of Dubai's real estate market, analyzing profitability, rental yields, capital appreciation, and liquidity trends using historical transactions and live market data.",
            ReportSection.AccessTier.FREE,
            1
        ),
        new DefaultSection(
            "area-performance",
            "Dubai Area Performance Analysis",
            "Area Performance Analysis",
            "A comparative investment analysis of Dubai's prime and emerging areas, identifying where investors can achieve the strongest returns based on yield, price growth, and demand dynamics.",
            ReportSection.AccessTier.FREE,
            2
        ),
        new DefaultSection(
            "property-type",
            "Property Type Investment Performance",
            "Property Type Performance",
            "A detailed performance review of apartments, villas, townhouses, and other asset classes to determine which property types deliver optimal income, appreciation, and exit liquidity.",
            ReportSection.AccessTier.FREE,
            3
        ),
        new DefaultSection(
            "property-size",
            "Property Size Investment Analysis",
            "Property Size Analysis",
            "A focused breakdown of performance by unit size, showing how different layouts and square footage impact rental income, resale value, and investor demand.",
            ReportSection.AccessTier.FREE,
            4
        ),
        new DefaultSection(
            "project-selection",
            "Project & Building Selection Intelligence",
            "Project Selection Intelligence",
            "A building-level intelligence report ranking specific projects and developments by profitability, rental stability, transaction velocity, and investor demand.",
            ReportSection.AccessTier.PREMIUM,
            5
        ),
        new DefaultSection(
            "price-negotiation",
            "Price Negotiation Intelligence",
            "Price Negotiation Intelligence",
            "A data-backed negotiation framework that benchmarks asking prices against historical transactions to define realistic purchase targets and negotiation margins.",
            ReportSection.AccessTier.PREMIUM,
            6
        ),
        new DefaultSection(
            "allocation-strategy",
            "Allocation Strategy by Budget",
            "Allocation by Budget",
            "A budget-driven investment strategy that matches capital ranges with the highest-performing areas, property types, and projects to optimize risk-adjusted returns.",
            ReportSection.AccessTier.ENTERPRISE,
            7
        )
    );

    @Override
    public void run(ApplicationArguments args) {
        List<String> languageCodes = new ArrayList<>();
        languageRepository.findByEnabledTrueOrderByNameAsc()
            .forEach(language -> languageCodes.add(language.getCode()));

        if (languageCodes.isEmpty()) {
            languageCodes.add("en");
        }

        for (String languageCode : languageCodes) {
            for (DefaultSection section : DEFAULTS) {
                reportSectionRepository.findBySectionKeyAndLanguageCode(section.key(), languageCode)
                    .orElseGet(() -> reportSectionRepository.save(ReportSection.builder()
                        .sectionKey(section.key())
                        .title(section.title())
                        .navTitle(section.navTitle())
                        .description(section.description())
                        .accessTier(section.accessTier())
                        .displayOrder(section.displayOrder())
                        .languageCode(languageCode)
                        .isActive(true)
                        .build()));
            }
        }
    }
}
