package com.example.claim_submission_workflow.claim.api;

import com.example.claim_submission_workflow.claim.application.ClaimSubmissionOrchestrator;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/claims")
public class ClaimController {

	private final ClaimSubmissionOrchestrator claimSubmissionOrchestrator;

	public ClaimController(ClaimSubmissionOrchestrator claimSubmissionOrchestrator) {
		this.claimSubmissionOrchestrator = claimSubmissionOrchestrator;
	}

	@PostMapping
	public ResponseEntity<ClaimResponse> submitClaim(@Valid @RequestBody SubmitClaimRequest request) {
		ClaimResponse response = claimSubmissionOrchestrator.submit(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public ClaimResponse getClaim(@PathVariable UUID id) {
		return claimSubmissionOrchestrator.findById(id)
				.orElseThrow(() -> new ClaimNotFoundException(id));
	}
}
