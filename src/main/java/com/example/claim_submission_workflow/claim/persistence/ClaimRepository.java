package com.example.claim_submission_workflow.claim.persistence;

import com.example.claim_submission_workflow.claim.domain.Claim;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {
}
