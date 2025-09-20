package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.security.SecurityPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SecurityPriceRepository extends JpaRepository<SecurityPrice, Long> {
    List<SecurityPrice> findAllBySecurityId(long securityId);

    List<SecurityPrice> findAllBySecurityIdIn(Set<Long> securityId);

    @Modifying
    void deleteAllBySecurityId(long securityId);

    @Query("select p.date from SecurityPrice p where p.securityId = :securityId order by p.date desc limit 1")
    Optional<LocalDate> findDateOfLatestPrice(long securityId);
}
