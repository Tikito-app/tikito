package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.security.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SecurityRepository extends JpaRepository<Security, Long> {
    List<Security> findBySecurityType(SecurityType securityType);

    Optional<Security> findByIdAndSecurityType(Long id, SecurityType securityType);

    @Modifying
    @Query("update Security s set s.lastPriceDate = :lastPriceDate where s.id = :id")
    void setLastPriceDate(long id, LocalDate lastPriceDate);
}
