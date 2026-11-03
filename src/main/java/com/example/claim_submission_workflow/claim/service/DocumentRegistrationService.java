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
public class DocumentRegistrationService {

	private static final Logger log = LoggerFactory.getLogger(DocumentRegistrationService.class);

	private final Set<UUID> registeredClaims = ConcurrentHashMap.newKeySet();
	private final String failAtStep;

	public DocumentRegistrationService(@Value("${claim.demo.fail-at-step:}") String failAtStep) {
		this.failAtStep = failAtStep;
	}

	public void register(Claim claim) {
		 log.info("Starting document registration for claim {}", claim.getId());

    if ("documents".equalsIgnoreCase(failAtStep)) {
        log.error("Document registration failed for claim {}", claim.getId());
        throw new WorkflowStepFailure("document registration");
    }

    registeredClaims.add(claim.getId());
    log.info("Demo document registration completed for claim {}", claim.getId());
		}

	public void compensate(Claim claim) {
		registeredClaims.remove(claim.getId());
		log.info("Demo document registration compensated for claim {}", claim.getId());
	}

	boolean isRegistered(UUID claimId) {
		return registeredClaims.contains(claimId);
	}

}
