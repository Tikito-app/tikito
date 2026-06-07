package org.tikito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.service.job.JobType;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobDto {

    private Long id;
    private Instant timestamp;
    private JobType jobType;
    private Long userId;
    private Long securityId;
    private Long accountId;
    private Long loanId;
}
