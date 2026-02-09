package com.ipomanagement.ipo_management_system.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "compliance_report")
public class ComplianceReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ipo_id")
    private Ipo ipo;

    @Column(nullable = false)
    private LocalDate generatedDate;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // getters/setters
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Ipo getIpo() { return ipo; }
    public void setIpo(Ipo ipo) { this.ipo = ipo; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}