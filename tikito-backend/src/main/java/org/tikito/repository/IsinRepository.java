package org.tikito.repository;

import org.tikito.entity.security.Isin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IsinRepository extends JpaRepository<Isin, String> {
}
