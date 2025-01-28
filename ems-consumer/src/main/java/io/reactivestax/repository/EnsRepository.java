package io.reactivestax.repository;

import io.reactivestax.domain.Ens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnsRepository extends JpaRepository<Ens, Long> {}