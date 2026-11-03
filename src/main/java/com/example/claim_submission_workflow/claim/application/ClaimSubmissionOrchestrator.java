package com.example.claim_submission_workflow.claim.application;

import com.example.claim_submission_workflow.claim.api.ClaimResponse;
import com.example.claim_submission_workflow.claim.api.SubmitClaimRequest;
import com.example.claim_submission_workflow.claim.domain.Claim;
import com.example.claim_submission_workflow.claim.persistence.ClaimRepository;
import com.example.claim_submission_workflow.claim.service.ClaimantNotificationService;
import com.example.claim_submission_workflow.claim.service.DocumentRegistrationService;
import com.example.claim_submission_workflow.claim.service.FraudScreeningService;
import com.example.claim_submission_workflow.claim.service.PolicyValidationService;
import com.example.claim_submission_workflow.claim.service.WorkflowStepFailure;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaimSubmissionOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(ClaimSubmissionOrchestrator.class);

	private final ClaimRepository claimRepository;
	private final PolicyValidationService policyValidationService;
	private final DocumentRegistrationService documentRegistrationService;
	private final FraudScreeningService fraudScreeningService;
	private final ClaimantNotificationService claimantNotificationService;

	public ClaimSubmissionOrchestrator(
			ClaimRepository claimRepository,
			PolicyValidationService policyValidationService,
			DocumentRegistrationService documentRegistrationService,
			FraudScreeningService fraudScreeningService,
			ClaimantNotificationService claimantNotificationService) {
		this.claimRepository = claimRepository;
		this.policyValidationService = policyValidationService;
		this.documentRegistrationService = documentRegistrationService;
		this.fraudScreeningService = fraudScreeningService;
		this.claimantNotificationService = claimantNotificationService;
	}

	public ClaimResponse submit(SubmitClaimRequest request) {
		Claim claim = Claim.receive(
				request.policyNumber(),
				request.claimantName(),
				request.claimAmount(),
				request.description());
		claimRepository.save(claim);
		claim.startProcessing();
		claimRepository.save(claim);

		List<Runnable> compensations = new ArrayList<>();
		try {
			runStep("policy validation", () -> policyValidationService.validate(claim));
			runStep("document registration", () -> documentRegistrationService.register(claim));
			compensations.add(() -> documentRegistrationService.compensate(claim));
			runStep("fraud screening", () -> fraudScreeningService.screen(claim));
			claim.markSubmitted();
		} catch (WorkflowStepFailure failure) {
			for (int index = compensations.size() - 1; index >= 0; index--) {
				try {
					compensations.get(index).run();
				} catch (RuntimeException compensationFailure) {
					failure.addSuppressed(compensationFailure);
					log.error("Compensation failed for claim {}", claim.getId(), compensationFailure);
				}
			}
			claim.markFailed(failure.getMessage());
			return toResponse(claimRepository.save(claim));
		}

		claimRepository.save(claim);
		try {
			runStep("claimant notification", () -> claimantNotificationService.notifyClaimant(claim));
		} catch (WorkflowStepFailure failure) {
			claim.markNotificationPending(failure.getMessage());
		}

		return toResponse(claimRepository.save(claim));
	}

	private static void runStep(String step, Runnable action) {
		try {
			action.run();
		} catch (RuntimeException failure) {
			throw new WorkflowStepFailure(step, failure);
		}
	}

	@Transactional(readOnly = true)
	public Optional<ClaimResponse> findById(UUID id) {
		return claimRepository.findById(id).map(ClaimSubmissionOrchestrator::toResponse);
	}

	private static ClaimResponse toResponse(Claim claim) {
		return new ClaimResponse(
				claim.getId(),
				claim.getPolicyNumber(),
				claim.getClaimantName(),
				claim.getClaimAmount(),
				claim.getDescription(),
				claim.getStatus(),
				claim.getFailureReason(),
				claim.getCreatedAt());
	}
}
