package io.reactivestax.repository;

import io.reactivestax.domain.Customer;
import io.reactivestax.domain.Ens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnsRepository extends JpaRepository<Ens, Long> {

    @Query(value = "SELECT * FROM ens WHERE id = :id", nativeQuery = true)
    Ens findCustomerDetailsById(@Param("id") Long id);
}