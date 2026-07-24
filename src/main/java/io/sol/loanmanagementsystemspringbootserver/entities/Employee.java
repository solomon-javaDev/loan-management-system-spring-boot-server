package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Employee extends User {
    private String firstName;
    private String lastName;
    private int salary;

    public Employee(
    ) {

    }

    public Employee(String firstName, String lastName, int salary, String email, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        super.setEmail(email);
        super.setRole(role);
        super.setUsername(firstName + " "+ lastName);
        super.setPassword("0000");
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return getId() != null && getId().equals(employee.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
