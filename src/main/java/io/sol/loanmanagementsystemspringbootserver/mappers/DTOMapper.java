package io.sol.loanmanagementsystemspringbootserver.mappers;

import io.sol.loanmanagementsystemspringbootserver.dtos.*;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Payment;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Utility class that provides methods to map entities to Data Transfer Objects (DTOs) and vice versa.
 * This class includes static methods to convert between entities and their respective DTOs:
 * - Customer ↔ CustomerCreateDTO/CustomerResponseDTO/CustomerDTO
 * - Loan ↔ LoanCreateDTO/LoanResponseDTO/LoanDTO
 * - Payment ↔ PaymentCreateDTO/PaymentResponseDTO/PaymentDTO
 * - Employee ↔ EmployeeCreateDTO/EmployeeResponseDTO/EmployeeDTO
 * The conversion process facilitates transferring data between application layers, specifically between
 * the domain model and the presentation layer.
 * Note:
 * - CreateDTOs are used for creating new entities (no ID field)
 * - ResponseDTOs are used for returning entity data to clients
 * - Old DTOs are kept for backward compatibility where needed
 */
public class DTOMapper {

    // ============== Customer Mappings ==============

    public static CustomerResponseDTO toResponseDTO(Customer customer) {
        if (customer == null) return null;
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(customer.getId());
        dto.setAccountNumber(customer.getAccountNumber());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setOtherNames(customer.getOtherNames());
        dto.setCustomerName(customer.getCustomerName());
        dto.setNin(customer.getNin());
        dto.setGuarantorName(customer.getGuarantorName());
        dto.setGuarantorPhone(customer.getGuarantorPhone());
        dto.setGuarantorNin(customer.getGuarantorNin());
        dto.setTelephone(customer.getTelephone());
        dto.setAddress(customer.getAddress());
        dto.setSavingsBalance(customer.getSavingsBalance());
        return dto;
    }

    public static Customer toEntity(CustomerCreateDTO dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setOtherNames(dto.getOtherNames());
        customer.setNin(dto.getNin());
        customer.setGuarantorName(dto.getGuarantorName());
        customer.setGuarantorPhone(dto.getGuarantorPhone());
        customer.setGuarantorNin(dto.getGuarantorNin());
        customer.setTelephone(dto.getTelephone());
        customer.setAddress(dto.getAddress());
        return customer;
    }

    public static CustomerDTO toDTO(Customer customer) {
        if (customer == null) return null;
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setAccountNumber(customer.getAccountNumber());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setOtherNames(customer.getOtherNames());
        dto.setCustomerName(customer.getCustomerName());
        dto.setNin(customer.getNin());
        dto.setGuarantorName(customer.getGuarantorName());
        dto.setGuarantorPhone(customer.getGuarantorPhone());
        dto.setGuarantorNin(customer.getGuarantorNin());
        dto.setTelephone(customer.getTelephone());
        dto.setAddress(customer.getAddress());
        dto.setSavingsBalance(customer.getSavingsBalance());
        dto.setActive(customer.isActive());
        dto.setFieldOfficer(toDTO(customer.getFieldOfficer()));
        return dto;
    }

    public static CustomerDTO toDTO(CustomerCreateDTO customer){
        if(customer == null) return null;

        CustomerDTO dto = new CustomerDTO();
        dto.setAccountNumber(customer.getAccountNumber());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setOtherNames(customer.getOtherNames());
        dto.setCustomerName(customer.getCustomerName());
        dto.setNin(customer.getNin());
        dto.setGuarantorName(customer.getGuarantorName());
        dto.setGuarantorPhone(customer.getGuarantorPhone());
        dto.setGuarantorNin(customer.getGuarantorNin());
        dto.setTelephone(customer.getTelephone());
        dto.setAddress(customer.getAddress());
        dto.setSavingsBalance(BigDecimal.ZERO);
        return dto;
    }

    // ============== Loan Mappings ==============

    // Backward compatibility - keep old methods
    public static LoanDTO toDTO(Loan loan) {
        if (loan == null) return null;
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setStartDate(loan.getStartDate());
        dto.setMaturityDate(loan.getMaturityDate());
        dto.setFullPaidDate(loan.getFullPaidDate());
        dto.setPrincipal(loan.getPrincipal());
        dto.setInterestRate(loan.getInterestRate());
        dto.setTenor(loan.getTenor());
        dto.setCollateral(loan.getCollateral());
        dto.setFees(loan.getFees());
        dto.setSurchargeRate(loan.getSurchargeRate());
        dto.setSurchargeAmount(loan.getSurchargeAmount());
        dto.setStatus(loan.getStatus());

        if (loan.getFieldOfficer() != null) {
            dto.setFieldOfficerId(loan.getFieldOfficer().getId());
            dto.setFieldOfficerName(loan.getFieldOfficer().getFirstName() + " " + loan.getFieldOfficer().getLastName());
        }

        if (loan.getGuarantor() != null) {
            dto.setGuarantorId((long) loan.getGuarantor().getId());
            dto.setGuarantorName(loan.getGuarantor().getFirstName() + " " + loan.getGuarantor().getLastName());
        }

        if (loan.getCustomer() != null) {
            dto.setCustomerId(loan.getCustomer().getId());
            dto.setCustomerName(loan.getCustomer().getFirstName() + " " + loan.getCustomer().getLastName());
        }

        dto.setTotalPaid(loan.getTotalPaid());
        dto.setOutstandingBalance(loan.getOutstandingBalance());
        dto.setTotalDue(loan.getTotalDue());
        dto.setFullPayment(loan.getFullPayment());
        dto.setReference(loan.getReference());
        dto.setAgingDays((int) loan.getAgingDays(LocalDate.now()));

        return dto;
    }

    public static Loan toEntity(LoanDTO dto) {
        if (dto == null) return null;
        Loan loan = new Loan();
        // Don't set ID - it should be generated
        loan.setStartDate(dto.getStartDate());
        loan.setMaturityDate(dto.getMaturityDate());
        loan.setFullPaidDate(dto.getFullPaidDate());
        loan.setPrincipal(dto.getPrincipal());
        loan.setInterestRate(dto.getInterestRate());
        loan.setTenor(dto.getTenor());
        loan.setCollateral(dto.getCollateral());
        loan.setFees(dto.getFees());
        loan.setStatus(dto.getStatus());
        // Relations should be handled by the service layer
        return loan;
    }

    // ============== Payment Mappings ==============

    // Backward compatibility - keeping old methods
    public static PaymentDTO toDTO(Payment payment) {
        if (payment == null) return null;
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setDate(payment.getDate());
        dto.setAmountReceived(payment.getAmountReceived());
        dto.setRemainingBalance(payment.getRemainingBalance());

        if (payment.getLoan() != null) {
            dto.setAgingDays(payment.getLoan().getAgingDays(LocalDate.now()));
            dto.setLoanId(payment.getLoan().getId());
            dto.setLoanReference(payment.getLoan().getReference());

            if (payment.getLoan().getCustomer() != null) {
                dto.setCustomerId(payment.getLoan().getCustomer().getId());
                dto.setCustomerName(payment.getLoan().getCustomer().getCustomerName());
            }
        }

        return dto;
    }

    // ============== Employee Mappings ==============

    // Backward compatibility - keep old methods
    public static EmployeeDTO toDTO(Employee employee) {
        if (employee == null) return null;
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setSalary(employee.getSalary());
        dto.setEmail(employee.getEmail());
        dto.setTelephone(employee.getTelephone());
        dto.setRole(employee.getRole());
        dto.setUsername(employee.getUsername());
        dto.setPassword(employee.getPassword());
        dto.setActive(employee.isActive());
        return dto;
    }

    public static Employee toEntity(EmployeeDTO dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        // Don't set ID - it should be generated or managed properly
        if (dto.getId() != null) {
            employee.setId(dto.getId());
        }
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setSalary(dto.getSalary());
        employee.setEmail(dto.getEmail());
        employee.setTelephone(dto.getTelephone());
        employee.setRole(dto.getRole());
        employee.setUsername(dto.getUsername());
        employee.setPassword(dto.getPassword());
        employee.setActive(dto.isActive());
        return employee;
    }

    public static EmployeeDTO toDTO(User user) {
        if (user == null) return null;
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPassword(user.getPassword());
        // TODO  For system users, we might not have first/last name or salary in the User entity,
        // but we can set them to empty or username if needed.
        dto.setFirstName(user.getUsername());
        dto.setActive(true);
        return dto;
    }
}
