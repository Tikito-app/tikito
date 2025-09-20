package org.tikito.repository;

import org.tikito.dto.security.SecurityType;
import org.tikito.entity.security.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityRepository extends JpaRepository<Security, Long> {
    List<Security> findBySecurityType(SecurityType securityType);
}
