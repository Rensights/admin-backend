package com.rensights.admin.repository;

import com.rensights.admin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByUserTier(User.UserTier userTier);
    long countByIsActive(boolean isActive);
    long countByEmailVerified(boolean emailVerified);
}

