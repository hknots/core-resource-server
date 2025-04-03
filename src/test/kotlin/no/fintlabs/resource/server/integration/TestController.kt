package no.fintlabs.resource.server.integration

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    fun testEndpoint(): ResponseEntity<String> {
        return ResponseEntity.ok("Success")
    }

}