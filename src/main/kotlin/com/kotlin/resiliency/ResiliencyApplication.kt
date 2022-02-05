package com.kotlin.resiliency

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ResiliencyApplication

fun main(args: Array<String>) {
	runApplication<ResiliencyApplication>(*args)
}

