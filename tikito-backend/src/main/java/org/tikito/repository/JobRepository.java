package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.Job;
import org.tikito.service.job.JobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {
    @Modifying
    void deleteByJobTypeAndUserId(JobType jobType, long userId);

    @Modifying
    void deleteByJobTypeAndUserIdAndSecurityId(JobType jobType, long userId, long securityId);

    @Modifying
    void deleteByJobTypeAndUserIdAndAccountId(JobType jobType, long userId, long accountId);

    @Query("select j from Job j order by j.id asc")
    List<Job> findAllOrdered();
}
