package com.kotlin.resiliency

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import com.kotlin.resiliency.StubbedResponses.aListOfPredefinedCustomers
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ContextConfiguration(classes = [ResiliencyApplicationTestsConfiguration::class])
@AutoConfigureMockMvc
class ResiliencyApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var wireMockServer: WireMockServer

	@Test
	fun getsOk() {

		wireMockServer.stubFor(
			get(urlEqualTo("/customers"))
				.willReturn(aListOfPredefinedCustomers())
		)

		mockMvc.perform(
			MockMvcRequestBuilders.get("/run")
		).andExpect(
			status().isOk
		).andExpect(
			MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
		)

	}

	@Test
	fun handlesAPIServerError() {
		wireMockServer.stubFor(
			get(urlEqualTo("/customers"))
				.willReturn(aResponse().withStatus(500).withBody("<h1>Internal Server Error</h1>"))
		)

		mockMvc.perform(
			MockMvcRequestBuilders.get("/run")
		).andExpect(
			status().isBadGateway
		).andExpect(
			MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
		).andExpect(
			MockMvcResultMatchers.jsonPath("$.retryable", `is`(true))
		)
	}

	@Test
	fun handlesAPIClientError() {
		wireMockServer.stubFor(
			get(urlEqualTo("/customers"))
				.willReturn(aResponse().withStatus(400).withBody("<h1>Bad Request</h1>"))
		)

		mockMvc.perform(
			MockMvcRequestBuilders.get("/run")
		).andExpect(
			status().isBadGateway
		).andExpect(
			MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
		).andExpect(
			MockMvcResultMatchers.jsonPath("$.retryable", `is`(false))
		)
	}

	@Test
	fun opensACircuitBreaker() {
		wireMockServer.stubFor(
			get(urlEqualTo("/customers"))
				.inScenario("Fail after first successful call")
				.whenScenarioStateIs(STARTED)
				.willReturn(aListOfPredefinedCustomers())
				.willSetStateTo("FAILURE")
		)

		wireMockServer.stubFor(
			get(urlEqualTo("/customers"))
				.inScenario("Fail after first successful call")
				.whenScenarioStateIs("FAILURE")
				.willReturn(aResponse().withStatus(500).withBody("<h1>Internal Server Error</h1>"))
		)

		nextAPICallHasResponseMatching(status().isOk)
		nextAPICallHasResponseMatching(status().isBadGateway)
		nextAPICallHasResponseMatching(status().isBadGateway)
		nextAPICallHasResponseMatching(status().isBadGateway)
		nextAPICallHasResponseMatching(status().isBadGateway)
		nextAPICallHasResponseMatching(status().isBadGateway)
		nextAPICallHasResponseMatching(status().isServiceUnavailable) // 6th call, 50% of the last 6 calls have failed
		nextAPICallHasResponseMatching(status().isServiceUnavailable)
		nextAPICallHasResponseMatching(status().isServiceUnavailable)

		// Wait for the CircuitBreaker to go to HALF-OPEN
		Thread.sleep(1000)

		// Reset mock scenario so that a 200 OK is returned again
		wireMockServer.resetScenarios()
		nextAPICallHasResponseMatching(status().isOk)

	}

	private fun nextAPICallHasResponseMatching(resultMatcher: ResultMatcher) {
		mockMvc.perform(
			MockMvcRequestBuilders.get("/run")
		).andExpect(
			resultMatcher
		)
	}

}

