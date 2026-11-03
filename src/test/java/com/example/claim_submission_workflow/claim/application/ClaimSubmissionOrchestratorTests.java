package com.example.claim_submission_workflow.claim.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.claim_submission_workflow.claim.api.ClaimResponse;
import com.example.claim_submission_workflow.claim.api.SubmitClaimRequest;
import com.example.claim_submission_workflow.claim.domain.Claim;
import com.example.claim_submission_workflow.claim.domain.ClaimStatus;
import com.example.claim_submission_workflow.claim.persistence.ClaimRepository;
import com.example.claim_submission_workflow.claim.service.ClaimantNotificationService;
import com.example.claim_submission_workflow.claim.service.DocumentRegistrationService;
import com.example.claim_submission_workflow.claim.service.FraudScreeningService;
import com.example.claim_submission_workflow.claim.service.PolicyValidationService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ClaimSubmissionOrchestratorTests {

	@Test
	void compensationFailureStillMarksAndSavesClaimAsFailed() {
		ClaimRepository repository = mock(ClaimRepository.class);
		PolicyValidationService policyValidationService = mock(PolicyValidationService.class);
		DocumentRegistrationService documentRegistrationService = mock(DocumentRegistrationService.class);
		FraudScreeningService fraudScreeningService = mock(FraudScreeningService.class);
		ClaimantNotificationService claimantNotificationService = mock(ClaimantNotificationService.class);
		RuntimeException screeningFailure = new IllegalStateException("Screening unavailable");

		when(repository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));
		doThrow(screeningFailure).when(fraudScreeningService).screen(any(Claim.class));
		doThrow(new IllegalStateException("Document service unavailable"))
				.when(documentRegistrationService).compensate(any(Claim.class));

		ClaimSubmissionOrchestrator orchestrator = new ClaimSubmissionOrchestrator(
				repository,
				policyValidationService,
				documentRegistrationService,
				fraudScreeningService,
				claimantNotificationService);

		ClaimResponse response = orchestrator.submit(new SubmitClaimRequest(
				"POL-TEST",
				"Test Claimant",
				new BigDecimal("100.00"),
				"Test description"));

		assertThat(response.status()).isEqualTo(ClaimStatus.FAILED);
		assertThat(response.failureReason()).isEqualTo("The claim workflow failed during fraud screening.");
		verify(repository, times(3)).save(any(Claim.class));
		verify(documentRegistrationService).compensate(any(Claim.class));
		verify(fraudScreeningService, org.mockito.Mockito.never()).compensate(any(Claim.class));
		verify(claimantNotificationService, org.mockito.Mockito.never()).notifyClaimant(any(Claim.class));
	}
}
