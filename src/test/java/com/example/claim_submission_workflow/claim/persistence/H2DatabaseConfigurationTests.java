package com.example.claim_submission_workflow.claim.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class H2DatabaseConfigurationTests {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Environment environment;

	@Test
	void datasourceUsesConfiguredInMemoryH2Database() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			assertThat(connection.getMetaData().getURL())
					.startsWith("jdbc:h2:mem:claimsdb");
			assertThat(connection.getMetaData().getDriverName()).containsIgnoringCase("H2");
		}
	}

	@Test
	void h2ConsoleIsEnabledAtConfiguredPath() {
		assertThat(environment.getProperty("spring.h2.console.enabled", Boolean.class)).isTrue();
		assertThat(environment.getRequiredProperty("spring.h2.console.path")).isEqualTo("/h2-console");
	}
}
