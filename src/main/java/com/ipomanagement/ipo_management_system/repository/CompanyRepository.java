package com.ipomanagement.ipo_management_system.repository;

import com.ipomanagement.ipo_management_system.domain.entity.Company;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByIssuerUser(User issuerUser);
    boolean existsByRegistrationNumber(String registrationNumber);
}