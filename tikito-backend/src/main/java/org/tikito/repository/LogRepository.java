package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tikito.entity.Log;

public interface LogRepository extends JpaRepository<Log, Long> {
}
