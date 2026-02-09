package com.ipomanagement.ipo_management_system.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "allotment_run")
public class AllotmentRun {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allotmentRunId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ipo_id", unique = true)
    private Ipo ipo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by_admin_user_id")
    private User executedByAdminUser;

    @Column(nullable = false)
    private String method = "LOTTERY";

    @Column(nullable = false)
    private Long randomSeed;

    @Column(nullable = false)
    private Instant executedAt = Instant.now();

    @Column(nullable = false, length = 30)
    private String status = "COMPLETED";

    // getters/setters
    public Long getAllotmentRunId() { return allotmentRunId; }
    public void setAllotmentRunId(Long allotmentRunId) { this.allotmentRunId = allotmentRunId; }

    public Ipo getIpo() { return ipo; }
    public void setIpo(Ipo ipo) { this.ipo = ipo; }

    public User getExecutedByAdminUser() { return executedByAdminUser; }
    public void setExecutedByAdminUser(User executedByAdminUser) { this.executedByAdminUser = executedByAdminUser; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Long getRandomSeed() { return randomSeed; }
    public void setRandomSeed(Long randomSeed) { this.randomSeed = randomSeed; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}