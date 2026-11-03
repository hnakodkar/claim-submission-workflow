package com.example.claim_submission_workflow.claim.service;

public class WorkflowStepFailure extends RuntimeException {

	public WorkflowStepFailure(String step) {
		super("The claim workflow failed during " + step + ".");
	}

	public WorkflowStepFailure(String step, Throwable cause) {
		super("The claim workflow failed during " + step + ".", cause);
	}
}
