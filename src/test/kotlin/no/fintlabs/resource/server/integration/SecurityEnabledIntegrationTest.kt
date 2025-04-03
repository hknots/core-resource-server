package no.fintlabs.resource.server.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    properties = [
        "fint.security.enabled=true",
    ]
)
@AutoConfigureWebTestClient
class SecurityEnabledIntegrationTest(
    @Autowired val webTestClient: WebTestClient
) {

    @Test
    fun `test with valid mock JWT`() {
        webTestClient
            .mutateWith(mockJwt().jwt { jwt ->
                jwt.claim("sub", "my-test-subject")
            })
            .get()
            .uri("/test")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .isEqualTo("Success")
    }

}
