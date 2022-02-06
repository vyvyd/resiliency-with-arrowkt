package com.kotlin.resiliency

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.kotlin.resiliency.StubbedResponses.aListOfPredefinedCustomers
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

		wireMockServer.stop()
	}




}

