package com.rensights.admin.repository;

import com.rensights.admin.model.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    /**
     * Existing row for a name, so importing the same file twice updates nothing rather than
     * duplicating it. Case-insensitive: "Burj Vista" and "BURJ VISTA" are the same building.
     */
    @Query("SELECT b FROM Building b WHERE LOWER(b.name) = :name")
    Optional<Building> findByNameIgnoringCase(@Param("name") String name);

    /**
     * The catalogue, filtered and ordered A-Z.
     *
     * <p>Sorted here on LOWER(name) instead of leaving it to the Pageable: Postgres' default
     * collation puts uppercase first, so an all-caps entry would jump the alphabet.
     */
    @Query("SELECT b FROM Building b "
        + "WHERE :query = '' OR LOWER(b.name) LIKE CONCAT('%', :query, '%') "
        + "ORDER BY LOWER(b.name) ASC")
    Page<Building> search(@Param("query") String query, Pageable pageable);
}
