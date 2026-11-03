package com.example.claim_submission_workflow.claim.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claims")
public class Claim {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String policyNumber;

	@Column(nullable = false)
	private String claimantName;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal claimAmount;

	@Column(nullable = false, length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ClaimStatus status;

	@Column(length = 500)
	private String failureReason;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Claim() {
	}

	private Claim(String policyNumber, String claimantName, BigDecimal claimAmount, String description) {
		this.id = UUID.randomUUID();
		this.policyNumber = policyNumber;
		this.claimantName = claimantName;
		this.claimAmount = claimAmount;
		this.description = description;
		this.status = ClaimStatus.RECEIVED;
		this.createdAt = Instant.now();
	}

	public void startProcessing() {
		this.status = ClaimStatus.PROCESSING;
		this.failureReason = null;
	}

	public void markSubmitted() {
		this.status = ClaimStatus.SUBMITTED;
		this.failureReason = null;
	}

	public void markNotificationPending(String reason) {
		this.status = ClaimStatus.SUBMITTED_NOTIFICATION_PENDING;
		this.failureReason = reason;
	}

	public void markFailed(String reason) {
		this.status = ClaimStatus.FAILED;
		this.failureReason = reason;
	}

	public static Claim receive(
			String policyNumber,
			String claimantName,
			BigDecimal claimAmount,
			String description) {
		return new Claim(policyNumber, claimantName, claimAmount, description);
	}

	public UUID getId() {
		return id;
	}

	public String getPolicyNumber() {
		return policyNumber;
	}

	public String getClaimantName() {
		return claimantName;
	}

	public BigDecimal getClaimAmount() {
		return claimAmount;
	}

	public String getDescription() {
		return description;
	}

	public ClaimStatus getStatus() {
		return status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
