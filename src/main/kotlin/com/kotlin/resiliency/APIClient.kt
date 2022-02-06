package com.kotlin.resiliency

import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class APIClient(
	private val restTemplate: RestTemplate
) {
	fun getCustomers(): List<DTOs.Customer>  {

		val response = restTemplate.exchange(
			"/customers",
            HttpMethod.GET,
			null,
			DTOs.ExternalAPIResponse::class.java,
		)
		return response.body!!.data
	}
}