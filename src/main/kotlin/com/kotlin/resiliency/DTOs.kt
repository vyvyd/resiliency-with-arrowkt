package com.kotlin.resiliency

object DTOs {

	data class Customer(
        val id: String,
        val url: String
    )

    data class ExternalAPIResponse(
        val data: List<Customer>
    )

    data class APIResponse(
        val status: Int,
        val customers: List<Customer>
    )

}