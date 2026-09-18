package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * The UserRepository interface extends JpaRepository to provide basic CRUD operations for User entities.   
 * It also includes a method to find a user by their username.
 * 
 * UserRepository
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
