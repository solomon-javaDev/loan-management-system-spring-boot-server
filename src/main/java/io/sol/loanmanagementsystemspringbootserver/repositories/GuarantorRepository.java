package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Guarantor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuarantorRepository extends JpaRepository<Guarantor, Long> {
    Optional<Guarantor> findByIdNumber(String idNumber);
}
