package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.security.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SecurityRepository extends JpaRepository<Security, Long> {
    @Query("select s from Security s where s.securityType in :securityTypes")
    List<Security> findBySecurityTypes(Set<SecurityType> securityTypes);

    @Query("select s from Security s where s.id = :id and s.securityType in :securityTypes")
    Optional<Security> findByIdAndSecurityTypes(Long id, Set<SecurityType> securityTypes);

    @Modifying
    @Query("update Security s set s.lastPriceDate = :lastPriceDate where s.id = :id")
    void setLastPriceDate(long id, LocalDate lastPriceDate);
}
