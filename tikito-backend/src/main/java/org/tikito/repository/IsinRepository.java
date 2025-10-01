package org.tikito.repository;

import org.tikito.entity.security.Isin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IsinRepository extends JpaRepository<Isin, String> {
    List<Isin> findBySecurityId(long securityId);
}
