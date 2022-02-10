package com.kotlin.resiliency

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import com.kotlin.resiliency.StubbedResponses.aListOfPredefinedCustomers
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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

	@AfterEach
	fun afterEach() {
		wireMockServer.resetMappings()
		wireMockServer.resetScenarios()
		wireMockServer.resetAll()
	}

	@Test
	fun getsOk() {
		wireMockServer.stubFor(
			get(urlEqualTo("/customers"))
				.willReturn(aListOfPredefinedCustomers())
		)

		mockMvc.perform(
			get("/run")
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
			get("/run")
		).andExpect(
			status().isBadGateway
		).andExpect(
			MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
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
			get("/run")
		).andExpect(
			status().isBadGateway
		).andExpect(
			MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
		).andExpect(
			MockMvcResultMatchers.jsonPath("$.retryable", `is`(false))
		)
	}

	@Test
	fun aFlakyAPICanBeHandledByTheClient() {
		wireMockServer.stubFor(
				get(urlEqualTo("/customers"))
						.inScenario("Fail after first successful call 1")
						.whenScenarioStateIs(STARTED)
						.willReturn(aListOfPredefinedCustomers())
						.willSetStateTo("TOGGLE FAILURE")
		)

		wireMockServer.stubFor(
				get(urlEqualTo("/customers"))
						.inScenario("Fail after first successful call 1")
						.whenScenarioStateIs("TOGGLE FAILURE")
						.willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
						.willSetStateTo(STARTED) // next call should succeed
		)

		nextApiCallHasResult(get("/run"), status().isOk)
		nextApiCallHasResult(get("/run"), status().isOk)
		wireMockServer.resetScenarios()

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

		nextApiCallHasResult(get("/run"), status().isOk)
		nextApiCallHasResult(get("/run"), status().isBadGateway)
		nextApiCallHasResult(get("/run"), status().isBadGateway)
		nextApiCallHasResult(get("/run"), status().isBadGateway)
		nextApiCallHasResult(get("/run"), status().isServiceUnavailable) // 6th call, 50% of the last 6 calls have failed
		nextApiCallHasResult(get("/run"), status().isServiceUnavailable)
		nextApiCallHasResult(get("/run"), status().isServiceUnavailable)

		// Wait for the CircuitBreaker to go to HALF-OPEN
		Thread.sleep(1000)

		// Reset mock scenario so that a 200 OK is returned again
		wireMockServer.resetScenarios()
		nextApiCallHasResult(get("/run"), status().isOk)
	}

	private fun nextApiCallHasResult(mockHttpServletRequestBuilder: MockHttpServletRequestBuilder, resultMatcher: ResultMatcher) {
		mockMvc.perform(
				mockHttpServletRequestBuilder
		).andExpect(
			resultMatcher
		)
	}

}

