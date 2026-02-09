package com.ipomanagement.ipo_management_system.repository;

import com.ipomanagement.ipo_management_system.domain.entity.ComplianceReport;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {
    List<ComplianceReport> findByIpoOrderByCreatedAtDesc(Ipo ipo);
}