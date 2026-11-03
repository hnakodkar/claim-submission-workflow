package com.example.claim_submission_workflow.claim.service;

import com.example.claim_submission_workflow.claim.domain.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClaimantNotificationService {

	private static final Logger log = LoggerFactory.getLogger(ClaimantNotificationService.class);

	private final String failAtStep;

	public ClaimantNotificationService(@Value("${claim.demo.fail-at-step:}") String failAtStep) {
		this.failAtStep = failAtStep;
	}

	public void notifyClaimant(Claim claim) {
		 log.info("Starting claimant notification for claim {}", claim.getId());

    if ("notification".equalsIgnoreCase(failAtStep)) {
        log.error("Claimant notification failed for claim {}", claim.getId());
        throw new WorkflowStepFailure("claimant notification");
    }

    log.info("Demo submission notification sent for claim {}", claim.getId());
	
	}

}