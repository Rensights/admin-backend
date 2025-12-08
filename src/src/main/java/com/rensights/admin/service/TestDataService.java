package com.rensights.admin.service;

import com.rensights.admin.model.Deal;
import com.rensights.admin.repository.DealRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class TestDataService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDataService.class);
    
    @Autowired
    private DealRepository dealRepository;
    
    @Override
    @Transactional
    public void run(String... args) {
        // Check if we should seed test data (only if database is empty)
        if (dealRepository.count() == 0) {
            logger.info("Seeding test deals into database...");
            seedTestDeals();
            logger.info("Test deals seeded successfully!");
        } else {
            logger.info("Database already contains deals, skipping seed.");
        }
    }
    
    public void seedTestDeals() {
        List<Deal> testDeals = Arrays.asList(
            // Dubai - Marina Area - PENDING deals
            Deal.builder()
                .name("Luxury Marina Apartment")
                .location("Dubai Marina, Building A")
                .city("Dubai")
                .area("Marina")
                .bedrooms("2")
                .bedroomCount("2")
                .size("1200 sqft")
                .listedPrice("AED 2,500,000")
                .priceValue(new BigDecimal("2500000"))
                .estimateMin(new BigDecimal("2400000"))
                .estimateMax(new BigDecimal("2600000"))
                .estimateRange("AED 2.4M - 2.6M")
                .discount("5%")
                .rentalYield("7.5%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            Deal.builder()
                .name("Marina Waterfront Studio")
                .location("Dubai Marina, Building B")
                .city("Dubai")
                .area("Marina")
                .bedrooms("1")
                .bedroomCount("1")
                .size("650 sqft")
                .listedPrice("AED 1,200,000")
                .priceValue(new BigDecimal("1200000"))
                .estimateMin(new BigDecimal("1150000"))
                .estimateMax(new BigDecimal("1250000"))
                .estimateRange("AED 1.15M - 1.25M")
                .discount("3%")
                .rentalYield("8.2%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            Deal.builder()
                .name("Marina Penthouse")
                .location("Dubai Marina, Tower C")
                .city("Dubai")
                .area("Marina")
                .bedrooms("3")
                .bedroomCount("3")
                .size("2500 sqft")
                .listedPrice("AED 5,800,000")
                .priceValue(new BigDecimal("5800000"))
                .estimateMin(new BigDecimal("5600000"))
                .estimateMax(new BigDecimal("6000000"))
                .estimateRange("AED 5.6M - 6.0M")
                .discount("7%")
                .rentalYield("6.8%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            // Dubai - Downtown - PENDING deals
            Deal.builder()
                .name("Burj Views Apartment")
                .location("Downtown Dubai, Emaar Building")
                .city("Dubai")
                .area("Downtown")
                .bedrooms("2")
                .bedroomCount("2")
                .size("1400 sqft")
                .listedPrice("AED 3,200,000")
                .priceValue(new BigDecimal("3200000"))
                .estimateMin(new BigDecimal("3100000"))
                .estimateMax(new BigDecimal("3300000"))
                .estimateRange("AED 3.1M - 3.3M")
                .discount("4%")
                .rentalYield("7.2%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            Deal.builder()
                .name("Downtown Luxury Loft")
                .location("Downtown Dubai, Tower D")
                .city("Dubai")
                .area("Downtown")
                .bedrooms("1")
                .bedroomCount("1")
                .size("900 sqft")
                .listedPrice("AED 2,100,000")
                .priceValue(new BigDecimal("2100000"))
                .estimateMin(new BigDecimal("2000000"))
                .estimateMax(new BigDecimal("2200000"))
                .estimateRange("AED 2.0M - 2.2M")
                .discount("6%")
                .rentalYield("7.8%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            // Dubai - JLT - APPROVED deals
            Deal.builder()
                .name("JLT Lake View Apartment")
                .location("Jumeirah Lakes Towers, Cluster A")
                .city("Dubai")
                .area("JLT")
                .bedrooms("2")
                .bedroomCount("2")
                .size("1100 sqft")
                .listedPrice("AED 1,800,000")
                .priceValue(new BigDecimal("1800000"))
                .estimateMin(new BigDecimal("1750000"))
                .estimateMax(new BigDecimal("1850000"))
                .estimateRange("AED 1.75M - 1.85M")
                .discount("2%")
                .rentalYield("8.5%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.APPROVED)
                .active(true)
                .batchDate(LocalDateTime.now().minusDays(1))
                .approvedAt(LocalDateTime.now().minusDays(1))
                .build(),
                
            Deal.builder()
                .name("JLT Business Bay View")
                .location("Jumeirah Lakes Towers, Cluster B")
                .city("Dubai")
                .area("JLT")
                .bedrooms("1")
                .bedroomCount("1")
                .size("750 sqft")
                .listedPrice("AED 1,050,000")
                .priceValue(new BigDecimal("1050000"))
                .estimateMin(new BigDecimal("1000000"))
                .estimateMax(new BigDecimal("1100000"))
                .estimateRange("AED 1.0M - 1.1M")
                .discount("5%")
                .rentalYield("9.0%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.APPROVED)
                .active(true)
                .batchDate(LocalDateTime.now().minusDays(2))
                .approvedAt(LocalDateTime.now().minusDays(2))
                .build(),
                
            // Dubai - Business Bay - OFF_PLAN deals
            Deal.builder()
                .name("Business Bay Off-Plan Tower")
                .location("Business Bay, New Development")
                .city("Dubai")
                .area("Business Bay")
                .bedrooms("3")
                .bedroomCount("3")
                .size("2000 sqft")
                .listedPrice("AED 4,500,000")
                .priceValue(new BigDecimal("4500000"))
                .estimateMin(new BigDecimal("4300000"))
                .estimateMax(new BigDecimal("4700000"))
                .estimateRange("AED 4.3M - 4.7M")
                .discount("10%")
                .rentalYield("6.5%")
                .buildingStatus(Deal.BuildingStatus.OFF_PLAN)
                .status(Deal.DealStatus.APPROVED)
                .active(true)
                .batchDate(LocalDateTime.now().minusDays(3))
                .approvedAt(LocalDateTime.now().minusDays(3))
                .build(),
                
            Deal.builder()
                .name("Business Bay Studio Off-Plan")
                .location("Business Bay, Premium Tower")
                .city("Dubai")
                .area("Business Bay")
                .bedrooms("1")
                .bedroomCount("1")
                .size("550 sqft")
                .listedPrice("AED 950,000")
                .priceValue(new BigDecimal("950000"))
                .estimateMin(new BigDecimal("900000"))
                .estimateMax(new BigDecimal("1000000"))
                .estimateRange("AED 900K - 1.0M")
                .discount("8%")
                .rentalYield("8.0%")
                .buildingStatus(Deal.BuildingStatus.OFF_PLAN)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            // Dubai - Palm Jumeirah - REJECTED deals
            Deal.builder()
                .name("Palm Jumeirah Villa")
                .location("Palm Jumeirah, Frond 5")
                .city("Dubai")
                .area("Palm Jumeirah")
                .bedrooms("4")
                .bedroomCount("4")
                .size("5000 sqft")
                .listedPrice("AED 15,000,000")
                .priceValue(new BigDecimal("15000000"))
                .estimateMin(new BigDecimal("14500000"))
                .estimateMax(new BigDecimal("15500000"))
                .estimateRange("AED 14.5M - 15.5M")
                .discount("3%")
                .rentalYield("5.5%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.REJECTED)
                .active(false)
                .batchDate(LocalDateTime.now().minusDays(5))
                .build(),
                
            Deal.builder()
                .name("Palm Apartment")
                .location("Palm Jumeirah, Tower 1")
                .city("Dubai")
                .area("Palm Jumeirah")
                .bedrooms("2")
                .bedroomCount("2")
                .size("1800 sqft")
                .listedPrice("AED 6,500,000")
                .priceValue(new BigDecimal("6500000"))
                .estimateMin(new BigDecimal("6300000"))
                .estimateMax(new BigDecimal("6700000"))
                .estimateRange("AED 6.3M - 6.7M")
                .discount("4%")
                .rentalYield("6.2%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.REJECTED)
                .active(false)
                .batchDate(LocalDateTime.now().minusDays(7))
                .build(),
                
            // Abu Dhabi deals
            Deal.builder()
                .name("Yas Island Apartment")
                .location("Yas Island, Residential Tower")
                .city("Abu Dhabi")
                .area("Yas Island")
                .bedrooms("2")
                .bedroomCount("2")
                .size("1300 sqft")
                .listedPrice("AED 2,200,000")
                .priceValue(new BigDecimal("2200000"))
                .estimateMin(new BigDecimal("2100000"))
                .estimateMax(new BigDecimal("2300000"))
                .estimateRange("AED 2.1M - 2.3M")
                .discount("5%")
                .rentalYield("7.0%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.PENDING)
                .active(true)
                .batchDate(LocalDateTime.now())
                .build(),
                
            Deal.builder()
                .name("Corniche Beachfront")
                .location("Corniche Area, Waterfront Building")
                .city("Abu Dhabi")
                .area("Corniche")
                .bedrooms("3")
                .bedroomCount("3")
                .size("2200 sqft")
                .listedPrice("AED 4,800,000")
                .priceValue(new BigDecimal("4800000"))
                .estimateMin(new BigDecimal("4600000"))
                .estimateMax(new BigDecimal("5000000"))
                .estimateRange("AED 4.6M - 5.0M")
                .discount("6%")
                .rentalYield("6.8%")
                .buildingStatus(Deal.BuildingStatus.READY)
                .status(Deal.DealStatus.APPROVED)
                .active(true)
                .batchDate(LocalDateTime.now().minusDays(1))
                .approvedAt(LocalDateTime.now().minusDays(1))
                .build()
        );
        
        dealRepository.saveAll(testDeals);
    }
}

