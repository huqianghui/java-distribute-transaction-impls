package com.seattle.msready.jta.atomikos.repository.customer;

import com.seattle.msready.jta.atomikos.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
