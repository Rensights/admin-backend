package com.rensights.admin.repository;

import com.rensights.admin.model.Subscription;
import com.rensights.admin.model.Subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);
    List<Subscription> findByUserId(UUID userId);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    long countByStatus(SubscriptionStatus status);
}

