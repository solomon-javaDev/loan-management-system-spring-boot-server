package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthenticationService provides methods for handling user authentication and password encoding.
 * It interacts with the UserService to validate user credentials and the PasswordEncoder to manage password encoding.
 * This service follows a result-based approach to represent success or failure conditions.
 */

@Service
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public Result<User> authenticate(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return Result.invalid("Please enter both username and password.", null);
        }

        Result<User> userResult = userService.findByUsername(username);
        if (userResult.isFailure() || userResult.value() == null) {
            return Result.unauthorized("Invalid username or password. Please try again.", null);
        }

        User user = userResult.value();
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Result.success("Login successful.", user);
        }

        return Result.unauthorized("Invalid username or password. Please try again.", null);
    }

    public Result<String> encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return Result.invalid("Password is required.", null);
        }

        return Result.success("Password encoded successfully.", passwordEncoder.encode(rawPassword));
    }
}
