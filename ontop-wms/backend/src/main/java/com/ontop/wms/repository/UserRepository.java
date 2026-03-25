package com.ontop.wms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ontop.wms.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds a user by their username and eagerly fetches the associated 'role' and 'warehouse' entities.
     * This is crucial to prevent LazyInitializationException in services like UserDetailsServiceImpl
     * and AuthServiceImpl, where these related objects are accessed outside the initial transaction.
     *
     * @param username the username to search for.
     * @return an Optional containing the User if found.
     */
    @EntityGraph(attributePaths = {"role", "warehouse"})
    Optional<User> findByUsername(String username);

    /**
     * Overrides the default findAll to eagerly fetch roles and warehouses, preventing N+1 query issues.
     */
    @EntityGraph(attributePaths = {"role", "warehouse"})
    List<User> findAll();
}