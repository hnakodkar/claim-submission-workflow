package com.example.claim_submission_workflow.claim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.claim_submission_workflow.claim.domain.Claim;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ClaimantNotificationServiceTests {

	@Test
	void notificationSucceedsWhenFailureIsNotConfigured() {
		ClaimantNotificationService service = new ClaimantNotificationService("");
		Claim claim = createClaim();

		assertThatCode(() -> service.notifyClaimant(claim)).doesNotThrowAnyException();
	}

	@Test
	void configuredNotificationFailureThrowsWorkflowStepFailure() {
		ClaimantNotificationService service = new ClaimantNotificationService("notification");
		Claim claim = createClaim();

		assertThatThrownBy(() -> service.notifyClaimant(claim))
				.isInstanceOf(WorkflowStepFailure.class)
				.hasMessage("The claim workflow failed during claimant notification.");
	}

	private static Claim createClaim() {
		return Claim.receive(
				"POL-NOTIFICATION-TEST",
				"Test Claimant",
				new BigDecimal("100.00"),
				"Test description");
	}
}
