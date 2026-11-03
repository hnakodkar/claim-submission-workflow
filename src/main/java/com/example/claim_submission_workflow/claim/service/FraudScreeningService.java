package com.example.claim_submission_workflow.claim.service;

import com.example.claim_submission_workflow.claim.domain.Claim;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FraudScreeningService {

	private static final Logger log = LoggerFactory.getLogger(FraudScreeningService.class);

	private final Set<UUID> screenedClaims = ConcurrentHashMap.newKeySet();
	private final String failAtStep;

	public FraudScreeningService(@Value("${claim.demo.fail-at-step:}") String failAtStep) {
		this.failAtStep = failAtStep;
	}

	public void screen(Claim claim) {
		log.info("Starting fraud screening for claim {}", claim.getId());

    if ("fraud".equalsIgnoreCase(failAtStep)) {
        log.error("Fraud screening failed for claim {}", claim.getId());
        throw new WorkflowStepFailure("fraud screening");
    }

    screenedClaims.add(claim.getId());
    log.info("Demo fraud screening completed for claim {}", claim.getId());
	}

	public void compensate(Claim claim) {
		screenedClaims.remove(claim.getId());
		log.info("Demo fraud screening compensated for claim {}", claim.getId());
	}

	boolean isScreened(UUID claimId) {
		return screenedClaims.contains(claimId);
	}

	
}
