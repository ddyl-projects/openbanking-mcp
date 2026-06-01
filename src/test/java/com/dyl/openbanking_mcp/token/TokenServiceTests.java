package com.dyl.openbanking_mcp.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTests {

	@Test
	void getToken_withValidToken_returnsToken() {
		DefaultTokenService service = new DefaultTokenService("my-test-token");
		assertThat(service.getToken()).isEqualTo("my-test-token");
	}

	@Test
	void getToken_withBlankToken_throwsIllegalStateException() {
		DefaultTokenService service = new DefaultTokenService("   ");
		assertThatThrownBy(service::getToken)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Authentication unavailable");
	}

	@Test
	void getToken_withEmptyToken_throwsIllegalStateException() {
		DefaultTokenService service = new DefaultTokenService("");
		assertThatThrownBy(service::getToken)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Authentication unavailable");
	}
}
