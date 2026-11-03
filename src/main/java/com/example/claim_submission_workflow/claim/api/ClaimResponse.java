package com.example.claim_submission_workflow.claim.api;

import com.example.claim_submission_workflow.claim.domain.ClaimStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClaimResponse(
		UUID id,
		String policyNumber,
		String claimantName,
		BigDecimal claimAmount,
		String description,
		ClaimStatus status,
		String failureReason,
		Instant createdAt) {
}
