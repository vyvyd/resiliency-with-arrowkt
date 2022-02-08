package com.kotlin.resiliency.external

object Beans {

    /**
     * External API DTO objects
     *
     * For now it is based on the dummy responses
     * available in the wiremock stubbings
     */
	data class Customer(
        val id: String,
        val url: String
    )

    data class ExternalAPIResponse(
        val data: List<Customer>
    )


}