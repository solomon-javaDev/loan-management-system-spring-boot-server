package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.User;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import org.springframework.stereotype.Service;

/**
 * The UserService class provides business logic related to user operations.
 * It interacts with the UserRepository to persist and retrieve user data.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<User> getUserById(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("A valid user id is required.", null);
        }

        return userRepository.findById(id)
                .map(user -> Result.success("User loaded successfully.", user))
                .orElseGet(() -> Result.notFound("User not found.", new User()));
    }

    public Result<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Result.invalid("Username is required.", null);
        }

        return userRepository.findByUsername(username)
                .map(user -> Result.success("User loaded successfully.", user))
                .orElseGet(() -> Result.notFound("User not found.", null));
    }

    public Result<User> save(User user) {
        if (user == null) {
            return Result.invalid("User details are required.", null);
        }

        return Result.success("User saved successfully.", userRepository.save(user));
    }
}
