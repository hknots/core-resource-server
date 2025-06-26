package no.fintlabs.resource.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class PingController {
    @GetMapping("/ping") fun ping() = Mono.just("pong")
}