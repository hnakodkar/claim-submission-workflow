package com.example.claim_submission_workflow.claim.service;

import com.example.claim_submission_workflow.claim.domain.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PolicyValidationService {

	private static final Logger log = LoggerFactory.getLogger(PolicyValidationService.class);

	private final String failAtStep;

	public PolicyValidationService(@Value("${claim.demo.fail-at-step:}") String failAtStep) {
		this.failAtStep = failAtStep;
	}

	public void validate(Claim claim) {
		log.info("Starting policy validation for claim {}", claim.getId());

    if ("policy".equalsIgnoreCase(failAtStep)) {
        log.error("Policy validation failed for claim {}", claim.getId());
        throw new WorkflowStepFailure("policy validation");
    }

    log.info("Demo policy validation passed for claim {}", claim.getId());
}
	
}
