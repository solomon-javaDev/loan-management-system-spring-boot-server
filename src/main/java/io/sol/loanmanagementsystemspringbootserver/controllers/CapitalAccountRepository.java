package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.CapitalAccount;
import org.springframework.data.repository.Repository;

import java.util.UUID;

interface CapitalAccountRepository extends Repository<CapitalAccount, UUID> {
}
