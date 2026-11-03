package com.example.claim_submission_workflow.claim.api;

import java.util.UUID;

public class ClaimNotFoundException extends RuntimeException {

	public ClaimNotFoundException(UUID id) {
		super("Claim " + id + " was not found.");
	}
}
