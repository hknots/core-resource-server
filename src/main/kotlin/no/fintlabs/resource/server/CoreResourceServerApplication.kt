package no.fintlabs.resource.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoreResourceServerApplication

fun main(args: Array<String>) {
	runApplication<CoreResourceServerApplication>(*args)
}
