package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "expense_categories")
@SQLDelete(sql = "UPDATE expense_categories SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(unique = true)
    private String description;

    public ExpenseCategory(String description){
        this.description = description;
    }


}
