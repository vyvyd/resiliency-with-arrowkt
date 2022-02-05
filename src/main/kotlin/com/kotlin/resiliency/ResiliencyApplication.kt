package com.kotlin.resiliency

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@SpringBootApplication
class ResiliencyApplication

fun main(args: Array<String>) {
	runApplication<ResiliencyApplication>(*args)
}

@Controller
class Endpoint {

	@GetMapping("/run")
	fun run() : ResponseEntity<String> {
		return ResponseEntity.ok("Ok")
	}
}