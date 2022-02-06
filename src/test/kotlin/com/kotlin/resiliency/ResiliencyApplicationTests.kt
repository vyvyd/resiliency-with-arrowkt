package com.kotlin.resiliency

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.kotlin.resiliency.StubbedResponses.aListOfPredefinedCustomers
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
			MockMvcResultMatchers.status().isOk
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
			MockMvcResultMatchers.status().isBadGateway
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
			MockMvcResultMatchers.status().isBadGateway
		).andExpect(
			MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
		).andExpect(
			MockMvcResultMatchers.jsonPath("$.retryable", `is`(false))
		)
	}

}

