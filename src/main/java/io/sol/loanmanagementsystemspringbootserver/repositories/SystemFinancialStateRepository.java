package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemFinancialStateRepository extends JpaRepository<SystemFinancialState,Long> {

    default SystemFinancialState findCurrentState(){
        return findById(1L).orElseThrow(()-> new IllegalStateException("System has not been initialised"));
    }

    Optional<SystemFinancialState> findFirstById(long id);
}
