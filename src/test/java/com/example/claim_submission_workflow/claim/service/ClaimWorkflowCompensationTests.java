package com.example.claim_submission_workflow.claim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "claim.demo.fail-at-step=fraud")
@AutoConfigureMockMvc
class ClaimWorkflowCompensationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DocumentRegistrationService documentRegistrationService;

	@Test
	void fraudFailureCompensatesDocumentRegistration() throws Exception {
		MvcResult result = mockMvc.perform(post("/claims")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "policyNumber": "POL-456",
								  "claimantName": "Alex Smith",
								  "claimAmount": 500.00,
								  "description": "Property damage"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		String location = result.getResponse().getHeader("Location");
		String path = URI.create(location).getPath();
		String claimId = path.substring(path.lastIndexOf('/') + 1);

		mockMvc.perform(get("/claims/{id}", claimId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FAILED"))
				.andExpect(jsonPath("$.failureReason")
						.value("The claim workflow failed during fraud screening."));

		assertThat(documentRegistrationService.isRegistered(UUID.fromString(claimId))).isFalse();
	}
}
