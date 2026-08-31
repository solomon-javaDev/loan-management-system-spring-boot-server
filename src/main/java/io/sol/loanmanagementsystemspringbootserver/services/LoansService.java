package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.repositories.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class that provides operations for managing loans and their associations
 * with customers. The class interacts with the underlying data repositories to
 * perform CRUD operations on loan entities and provides functionality such as
 * issuing loans, updating loan details, and validating input data.
 */
@Service
public class LoansService {


    private final LoansRepository loansRepository;

    public LoansService(LoansRepository loansRepository) {
        this.loansRepository = loansRepository;
    }

    public Result<List<LoanDTO>> getAllLoans() {
        return Result.success("Loans loaded successfully.", 
            loansRepository.findAll().stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    public Result<LoanDTO> getLoanById(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("A valid loan id is required.", null);
        }

        return loansRepository.findById(id)
                .map(loan -> Result.success("Loan loaded successfully.", DTOMapper.toDTO(loan)))
                .orElseGet(() -> Result.notFound("Loan not found.", null));
    }

    public Optional<Loan> getLoanEntityById(Integer id) {
        return loansRepository.findById(id);
    }

    @Transactional
    public Loan saveLoanEntity(Loan loan) {
        return loansRepository.save(loan);
    }


    @Transactional
    public Result<LoanDTO> issueLoan(int customerId, LoanDTO loanDto) {
        return Result.notFound("Not found", new LoanDTO());
    }


    @Transactional
    public Result<LoanDTO> updateLoan(LoanDTO loanDto) {
        return Result.notFound("Not found", new LoanDTO());
    }


    public Result<Void> deleteLoan(Integer id) {
        if (id == null || id <= 0) {
            return Result.invalid("Select a loan from the table before deleting.", null);
        }

        if (!loansRepository.existsById(id)) {
            return Result.notFound("Loan not found.", null);
        }

        loansRepository.deleteById(id);
        return Result.success("Loan deleted successfully.", null);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1 AM
    @Transactional
    public void updateOverdueLoansSurcharges() {

    }
}
