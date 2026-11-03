package com.example.claim_submission_workflow.claim.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkflowStepFailureTests {

	@Test
	void retainsOriginalExceptionAsCause() {
		IllegalStateException originalFailure = new IllegalStateException("Service unavailable");

		WorkflowStepFailure failure = new WorkflowStepFailure("fraud screening", originalFailure);

		assertThat(failure).hasCauseReference(originalFailure);
	}
}
