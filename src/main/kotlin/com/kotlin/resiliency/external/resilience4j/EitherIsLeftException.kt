package com.kotlin.resiliency

import com.kotlin.resiliency.external.ExternalAPIError

class EitherIsLeftException(val leftValue: ExternalAPIError) : RuntimeException()