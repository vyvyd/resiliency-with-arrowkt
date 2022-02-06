package com.kotlin.resiliency

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ResiliencyApplication

fun main(args: Array<String>) {
	runApplication<ResiliencyApplication>(*args)
}

@Configuration
class DefaultConfiguration {

	@Bean
	fun restTemplate(): RestTemplate {
		return RestTemplate()
	}

	@Bean
	fun apiClient(restTemplate: RestTemplate): APIClient {
		return ResilientAPIClient(
			DefaultAPIClient(restTemplate)
		)
	}

}

