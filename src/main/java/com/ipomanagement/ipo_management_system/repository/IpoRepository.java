package com.ipomanagement.ipo_management_system.repository;

import com.ipomanagement.ipo_management_system.domain.entity.Company;
import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.domain.enums.IpoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IpoRepository extends JpaRepository<Ipo, Long> {

    List<Ipo> findByCompany(Company company);

    // avoid lazy issues in Thymeleaf
    @Query("select i from Ipo i join fetch i.company where i.ipoId = :id")
    Optional<Ipo> findByIdWithCompany(@Param("id") Long id);

    @Query("select i from Ipo i join fetch i.company order by i.ipoId desc")
    List<Ipo> findAllWithCompany();

    @Query("select i from Ipo i join fetch i.company where i.status = :status order by i.ipoId desc")
    List<Ipo> findByStatusWithCompany(@Param("status") IpoStatus status);
}