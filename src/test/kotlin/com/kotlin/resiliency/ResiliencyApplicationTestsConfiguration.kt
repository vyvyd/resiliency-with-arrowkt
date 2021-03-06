package com.kotlin.resiliency

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory

@TestConfiguration
class ResiliencyApplicationTestsConfiguration {

	@Bean
	@Scope("singleton")
	fun wireMockServer(): WireMockServer {
		return WireMockServer(9087).also{
			it.start()
		}
	}

	@Bean
	@Primary
	fun restOperations(wireMockServer: WireMockServer) : RestOperations {
		val factory = DefaultUriBuilderFactory(wireMockServer.baseUrl())
		return RestTemplate().also {
			it.uriTemplateHandler = factory
		}
	}
}