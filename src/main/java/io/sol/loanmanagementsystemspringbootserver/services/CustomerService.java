package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    public CustomerService(){

    }

    /*
    Create
    Update
    Read
    Delete
     */

    public void createCustomer(Customer customer){
        repository.save(customer);
    }

    @Transactional //dirty checking of the database
    public void updateCustomer(Customer customer){
       repository.findById(customer.getId()).ifPresent(existingCustomer -> {
           existingCustomer.setEmail(customer.getEmail());
           existingCustomer.setFirstName(customer.getFirstName());
           existingCustomer.setLastName(customer.getLastName());
           existingCustomer.setLastName(customer.getTelephone());
           existingCustomer.setOtherNames(customer.getOtherNames());
       });
    }

    public void deleteCustomer(Customer customer){
        repository.deleteById(customer.getId());
    }
}
