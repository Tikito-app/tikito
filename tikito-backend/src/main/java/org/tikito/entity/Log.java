package org.tikito.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.LogDto;
import org.tikito.dto.LogMessage;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Log {
    @Id
    private Long id;
    private Instant timestamp;
    private String objectIdentifier;
    @Enumerated(EnumType.STRING)
    private LogMessage message;

    public Log(final LogMessage message, final String objectIdentifier) {
        this.timestamp = Instant.now();
        this.message = message;
        this.objectIdentifier = objectIdentifier;
    }

    public LogDto toDto() {
        return new LogDto(id, timestamp, objectIdentifier, message);
    }
}
