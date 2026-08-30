package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tikito.entity.security.Isin;

import java.util.List;

public interface IsinRepository extends JpaRepository<Isin, String> {
    List<Isin> findBySecurityId(long securityId);
}
