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
import java.util.HashSet;
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
        long dealCount = dealRepository.count();
        if (dealCount == 0) {
            logger.info("Seeding test deals into database...");
            seedTestDeals();
            logger.info("Test deals seeded successfully!");
        } else {
            logger.info("Database already contains {} deals, skipping auto-seed.", dealCount);
            
            // Check if deals have relationships, if not, add them
            boolean hasRelationships = dealRepository.findAll().stream()
                    .anyMatch(d -> (d.getListedDeals() != null && !d.getListedDeals().isEmpty()) || 
                                  (d.getRecentSales() != null && !d.getRecentSales().isEmpty()));
            
            if (!hasRelationships) {
                logger.info("No deal relationships found. Adding relationships to existing deals...");
                addRelationshipsToExistingDeals();
            }
        }
    }
    
    @Transactional
    public void seedTestDeals() {
        // First, save all deals without relationships
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
        
        List<Deal> savedDeals = dealRepository.saveAll(testDeals);
        
        // Now establish relationships between deals
        if (savedDeals.size() >= 5) {
            logger.info("Establishing deal relationships...");
            
            // Get approved deals for relationships
            List<Deal> approvedDeals = savedDeals.stream()
                    .filter(d -> d.getStatus() == Deal.DealStatus.APPROVED && d.getActive())
                    .collect(java.util.stream.Collectors.toList());
            
            int relationshipsCreated = 0;
            
            // Create relationships for multiple approved deals (up to 5)
            int dealsToProcess = Math.min(approvedDeals.size(), 5);
            
            for (int i = 0; i < dealsToProcess; i++) {
                Deal mainDeal = approvedDeals.get(i);
                
                // Get other approved deals for listed deals
                List<Deal> otherApproved = approvedDeals.stream()
                        .filter(d -> !d.getId().equals(mainDeal.getId()))
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
                
                // Add listed deals
                for (Deal listedDeal : otherApproved) {
                    if (!mainDeal.getListedDeals().contains(listedDeal)) {
                        mainDeal.getListedDeals().add(listedDeal);
                    }
                }
                
                // Get pending deals for recent sales
                List<Deal> pendingDeals = savedDeals.stream()
                        .filter(d -> d.getStatus() == Deal.DealStatus.PENDING && 
                                    d.getActive() && 
                                    !d.getId().equals(mainDeal.getId()))
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
                
                // Add recent sales
                for (Deal recentSale : pendingDeals) {
                    if (!mainDeal.getRecentSales().contains(recentSale) &&
                        !mainDeal.getListedDeals().contains(recentSale)) {
                        mainDeal.getRecentSales().add(recentSale);
                    }
                }
                
                // If not enough pending deals, use other approved deals
                if (mainDeal.getRecentSales().size() < 2 && approvedDeals.size() > otherApproved.size() + 1) {
                    List<Deal> additionalRecentSales = approvedDeals.stream()
                            .filter(d -> !d.getId().equals(mainDeal.getId()) &&
                                        !otherApproved.stream().anyMatch(od -> od.getId().equals(d.getId())))
                            .limit(2)
                            .collect(java.util.stream.Collectors.toList());
                    
                    for (Deal recentSale : additionalRecentSales) {
                        if (!mainDeal.getRecentSales().contains(recentSale) &&
                            !mainDeal.getListedDeals().contains(recentSale)) {
                            mainDeal.getRecentSales().add(recentSale);
                        }
                    }
                }
                
                dealRepository.save(mainDeal);
                relationshipsCreated++;
                logger.info("✅ Added relationships to deal: {} ({} listed deals, {} recent sales)", 
                        mainDeal.getName(), 
                        mainDeal.getListedDeals().size(), 
                        mainDeal.getRecentSales().size());
            }
            
            logger.info("✅ Deal relationships established successfully for {} deals!", relationshipsCreated);
        }
    }
    
    /**
     * Add relationships to existing deals for testing
     */
    @Transactional
    public void addRelationshipsToExistingDeals() {
        List<Deal> allDeals = dealRepository.findAll();
        
        if (allDeals.size() < 5) {
            logger.warn("Not enough deals to create relationships. Need at least 5 deals.");
            return;
        }
        
        // Find approved and active deals (better candidates for relationships)
        List<Deal> candidateDeals = allDeals.stream()
                .filter(d -> d.getStatus() == Deal.DealStatus.APPROVED && d.getActive())
                .collect(java.util.stream.Collectors.toList());
        
        if (candidateDeals.size() < 3) {
            logger.warn("Not enough approved active deals to create relationships. Found: {}", candidateDeals.size());
            // Fall back to all approved deals
            candidateDeals = allDeals.stream()
                    .filter(d -> d.getStatus() == Deal.DealStatus.APPROVED)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        if (candidateDeals.size() < 2) {
            logger.warn("Not enough deals to create relationships.");
            return;
        }
        
        int relationshipsCreated = 0;
        
        // Create relationships for multiple deals (up to 5 deals)
        int dealsToProcess = Math.min(candidateDeals.size(), 5);
        
        for (int i = 0; i < dealsToProcess; i++) {
            Deal mainDeal = candidateDeals.get(i);
            
            // Skip if deal already has relationships
            if ((mainDeal.getListedDeals() != null && !mainDeal.getListedDeals().isEmpty()) ||
                (mainDeal.getRecentSales() != null && !mainDeal.getRecentSales().isEmpty())) {
                logger.debug("Deal {} already has relationships, skipping.", mainDeal.getName());
                continue;
            }
            
            // Clear existing relationships (if any)
            if (mainDeal.getListedDeals() == null) {
                mainDeal.setListedDeals(new HashSet<>());
            } else {
                mainDeal.getListedDeals().clear();
            }
            
            if (mainDeal.getRecentSales() == null) {
                mainDeal.setRecentSales(new HashSet<>());
            } else {
                mainDeal.getRecentSales().clear();
            }
            
            // Find other deals to link (excluding current deal)
            List<Deal> otherDeals = candidateDeals.stream()
                    .filter(d -> !d.getId().equals(mainDeal.getId()))
                    .collect(java.util.stream.Collectors.toList());
            
            // Add 2-3 listed deals
            int listedCount = Math.min(3, otherDeals.size());
            for (int j = 0; j < listedCount && j < otherDeals.size(); j++) {
                Deal listedDeal = otherDeals.get(j);
                if (!mainDeal.getListedDeals().contains(listedDeal)) {
                    mainDeal.getListedDeals().add(listedDeal);
                }
            }
            
            // Add 2-3 recent sales (use different deals if possible)
            List<Deal> pendingDeals = allDeals.stream()
                    .filter(d -> d.getStatus() == Deal.DealStatus.PENDING && 
                                d.getActive() && 
                                !d.getId().equals(mainDeal.getId()) &&
                                !otherDeals.stream().anyMatch(od -> od.getId().equals(d.getId())))
                    .limit(3)
                    .collect(java.util.stream.Collectors.toList());
            
            // Use pending deals first, then fall back to other approved deals
            List<Deal> recentSaleCandidates = pendingDeals.isEmpty() ? 
                    otherDeals.stream().skip(listedCount).limit(3).collect(java.util.stream.Collectors.toList()) :
                    pendingDeals;
            
            for (int j = 0; j < Math.min(3, recentSaleCandidates.size()); j++) {
                Deal recentSale = recentSaleCandidates.get(j);
                if (!mainDeal.getRecentSales().contains(recentSale) && 
                    !mainDeal.getListedDeals().contains(recentSale)) {
                    mainDeal.getRecentSales().add(recentSale);
                }
            }
            
            dealRepository.save(mainDeal);
            relationshipsCreated++;
            logger.info("✅ Added relationships to deal: {} ({} listed deals, {} recent sales)", 
                    mainDeal.getName(), 
                    mainDeal.getListedDeals().size(), 
                    mainDeal.getRecentSales().size());
        }
        
        logger.info("✅ Successfully created relationships for {} deals", relationshipsCreated);
    }
}

