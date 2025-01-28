package io.reactivestax.repository;

import io.reactivestax.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "SELECT * FROM customer WHERE id = :id", nativeQuery = true)
    Customer findCustomerDetailsById(@Param("id") Long id);
}