package io.sol.loanmanagementsystemspringbootserver.entities.custom;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@SQLDelete(sql = "update Customer SET deleted = true where id = ?")
@SQLRestriction("deleted = false")
@Setter @Getter
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean deleted = Boolean.FALSE;

    @Column(unique = true)
    private String accountNumber;

    @Column
    @NotNull(message = "First name cannot be empty")
    private String firstName;

    @Column
    @NotNull(message = "Last name cannot be empty")
    private String lastName;

    @Column
    private String otherNames;

    @Column
    private String customerName;

    @Column(unique = true)
    @NotBlank(message = "NIN is required")
    private String nin;

    @Column
    @NotBlank(message = "Telephone number is required")
    private String telephone;

    @Column
    private String address;

    private String guarantorName;

    private String guarantorPhone;

    private String guarantorNin;
    
    @Column(precision = 19, scale = 4)
    private java.math.BigDecimal savingsBalance = java.math.BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "field_officer_id")
    private Employee fieldOfficer;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();



    /*
    Methods to keep the loans and customer entities in sync because they're the backbone of the app
     */
    public  void addLoan(Loan loan){
        loans.add(loan);
        loan.setCustomer(this);
    }

    public void removeLoan(Loan loan){
        loans.remove(loan);
        loan.setCustomer(null);
    }


    public Customer() {
        this.customerName = firstName + " " + lastName + " " + otherNames;

    }

    public Customer(String firstName, String lastName, String otherNames, String nin, String telephone, String address) {
        this.customerName = firstName + " " + lastName + " " + otherNames;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.nin = nin;
        this.telephone = telephone;
        this.address = address;
    }



    public void setFirstName(String firstName) {
        this.firstName = firstName;
        refreshCustomerName();
    }



    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
        refreshCustomerName();
    }



  

    /**
     * Eligibility rule for a guarantor: a customer qualifies if they currently have no
     * outstanding (active/pending) loan, OR their savings balance is enough to cover their
     * own outstanding loan(s) plus the new loan principal they would guarantee.
     */
    public boolean canGuarantee(BigDecimal newLoanPrincipal) {
        BigDecimal exposure = loans.stream()
                .filter(l -> l != null && (l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.PENDING))
                .map(Loan::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (exposure.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }

        BigDecimal required = exposure.add(newLoanPrincipal == null ? BigDecimal.ZERO : newLoanPrincipal);
        BigDecimal savings = savingsBalance == null ? BigDecimal.ZERO : savingsBalance;
        return required.compareTo(savings) <= 0;
    }

    private void refreshCustomerName() {
        this.customerName = String.join(" ",
                firstName == null ? "" : firstName,
                lastName == null ? "" : lastName,
                otherNames == null ? "" : otherNames).trim().replaceAll(" +", " ");
    }
}
