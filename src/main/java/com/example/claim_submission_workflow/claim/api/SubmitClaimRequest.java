package com.example.claim_submission_workflow.claim.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record SubmitClaimRequest(
		@NotBlank String policyNumber,
		@NotBlank String claimantName,
		@NotNull @Positive BigDecimal claimAmount,
		@NotBlank String description) {
}
