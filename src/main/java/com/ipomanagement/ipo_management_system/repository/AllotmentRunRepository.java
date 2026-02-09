package com.ipomanagement.ipo_management_system.repository;

import com.ipomanagement.ipo_management_system.domain.entity.AllotmentRun;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllotmentRunRepository extends JpaRepository<AllotmentRun, Long> {
    Optional<AllotmentRun> findByIpo(Ipo ipo);
    boolean existsByIpo(Ipo ipo);
}