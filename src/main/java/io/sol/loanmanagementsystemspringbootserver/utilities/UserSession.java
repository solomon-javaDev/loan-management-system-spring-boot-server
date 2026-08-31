package io.sol.loanmanagementsystemspringbootserver.utilities;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import org.springframework.stereotype.Component;

@Component
public class UserSession {
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public boolean isCashier() {
        return currentUser != null && currentUser.getRole() == Role.CASHIER;
    }
}
