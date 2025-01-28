package io.reactivestax.repository;

import io.reactivestax.domain.Ens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnsRepository extends JpaRepository<Ens, Long> {}