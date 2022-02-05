package com.kotlin.resiliency

import com.kotlin.resiliency.AllConfigurations.*
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.client.RestTemplate

@Controller
class Endpoint(
	private val apiClient: ApiClient
) {

	@GetMapping("/run")
	fun run() : ResponseEntity<String> {
		val response = apiClient.call()
		return ResponseEntity.ok(response)
	}
}

@Configuration
class AllConfigurations {

	@Bean
	fun restTemplate(): RestTemplate {
		return RestTemplateBuilder().build()
	}

	@Component
	class ApiClient(
		private val restTemplate: RestTemplate
	) {

		fun call(): String  {
			return "remote + Ok"
		}

	}
}
