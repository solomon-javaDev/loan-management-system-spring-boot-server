package io.sol.loanmanagementsystemspringbootserver.utilities;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UserSession {
    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public boolean isCashier() {
        return currentUser != null && currentUser.getRole() == Role.CASHIER;
    }
}
