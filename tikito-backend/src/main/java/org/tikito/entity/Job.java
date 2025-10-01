package org.tikito.entity;

import jakarta.persistence.*;
import org.tikito.service.job.JobType;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant timestamp;
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    private Long userId;
    private Long securityId;
    private Long accountId;
    private Long loanId;

    public static JobBuilder create(final JobType jobType) {
        return Job.builder().jobType(jobType);
    }

    public static JobBuilder security(final JobType jobType, final long securityId) {
        return create(jobType)
                .securityId(securityId);
    }

    public static JobBuilder security(final JobType jobType, final long securityId, final long userId) {
        return create(jobType)
                .userId(userId)
                .securityId(securityId);
    }

    public static JobBuilder account(final JobType jobType, final long userId) {
        return create(jobType)
                .userId(userId);
    }

    public static JobBuilder account(final JobType jobType, final long accountId, final long userId) {
        return create(jobType)
                .userId(userId)
                .accountId(accountId);
    }

    public static JobBuilder loan(final JobType jobType, final long loanId, final long userId) {
        return create(jobType)
                .userId(userId)
                .loanId(loanId);
    }
}
