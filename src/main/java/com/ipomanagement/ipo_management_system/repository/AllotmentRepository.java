package com.ipomanagement.ipo_management_system.repository;

import com.ipomanagement.ipo_management_system.domain.entity.Allotment;
import com.ipomanagement.ipo_management_system.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllotmentRepository extends JpaRepository<Allotment, Long> {
    Optional<Allotment> findBySubscription(Subscription subscription);
}