package com.kotlin.resiliency

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class ResiliencyApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun getsOk() {
		mockMvc.perform(
			get("/run")
		).andExpect(
			MockMvcResultMatchers.status().isOk
		)
	}


}
