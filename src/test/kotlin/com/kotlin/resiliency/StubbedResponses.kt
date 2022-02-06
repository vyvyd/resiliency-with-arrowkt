package com.kotlin.resiliency

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.MediaType

object StubbedResponses {

	fun aListOfPredefinedCustomers(): ResponseDefinitionBuilder = WireMock.aResponse()
		.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
		.withBody("""
					{
					  "data": [
						{
						  "id": "1003214",
						  "url": "https://customers.acme.ink/og445614adsda"
						},
						{
						  "id": "453453",
						  "url": "https://customers.acme.ink/fdfsdf3423423fs"
						},
						{
						  "id": "676465",
						  "url": "https://customers.acme.ink/gtryrty322424d"
						},
						{
						  "id": "456432",
						  "url": "https://customers.acme.ink/jytbda34344sgrt"
						}
					  ]
					}
			""".trimIndent())

}