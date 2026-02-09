package com.ipomanagement.ipo_management_system.repository;

import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.entity.Subscription;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByIpoAndInvestorUser(Ipo ipo, User investorUser);

    List<Subscription> findByInvestorUserOrderByCreatedAtDesc(User investorUser);

    List<Subscription> findByIpoAndStatus(Ipo ipo, SubscriptionStatus status);

    // Fetch investorUser eagerly for allotment processing (prevents lazy problems)
    @Query("select s from Subscription s join fetch s.investorUser where s.ipo = :ipo and s.status = :status")
    List<Subscription> findByIpoAndStatusWithInvestor(@Param("ipo") Ipo ipo, @Param("status") SubscriptionStatus status);
}