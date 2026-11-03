package com.example.claim_submission_workflow.claim.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "claim.demo.fail-at-step=")
@AutoConfigureMockMvc
class ClaimControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void submitClaimReturnsCreatedClaimThatCanBeRetrieved() throws Exception {
		MvcResult submission = mockMvc.perform(post("/claims")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "policyNumber": "POL-123",
								  "claimantName": "Jane Doe",
								  "claimAmount": 1250.00,
								  "description": "Vehicle damage"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.status").value("SUBMITTED"))
				.andReturn();

		String location = submission.getResponse().getHeader("Location");
		String path = URI.create(location).getPath();
		String claimId = path.substring(path.lastIndexOf('/') + 1);
		Long persistedClaims = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM claims WHERE id = ?",
				Long.class,
				UUID.fromString(claimId));
		assertThat(persistedClaims).isEqualTo(1L);

		mockMvc.perform(get("/claims/{id}", claimId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.policyNumber").value("POL-123"))
				.andExpect(jsonPath("$.claimantName").value("Jane Doe"));
	}

	@Test
	void getClaimReturnsNotFoundForUnknownId() throws Exception {
		mockMvc.perform(get("/claims/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	@Test
	void getClaimRejectsMalformedId() throws Exception {
		mockMvc.perform(get("/claims/{id}", "not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
		mockMvc.perform(put("/claims"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.status").value(405));
	}

	@Test
	void submitClaimRejectsInvalidRequest() throws Exception {
		mockMvc.perform(post("/claims")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "policyNumber": "",
								  "claimantName": "Jane Doe",
								  "claimAmount": -1,
								  "description": "Vehicle damage"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.policyNumber").exists())
				.andExpect(jsonPath("$.errors.claimAmount").exists());
	}

	@Test
	void submitClaimRejectsMalformedJson() throws Exception {
		mockMvc.perform(post("/claims")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"policyNumber\":"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.status").value(400));
	}
}
