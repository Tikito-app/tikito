package org.tikito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LogDto {
    private Long id;
    private Instant timestamp;
    private String objectIdentifier;
    private LogMessage message;
}
