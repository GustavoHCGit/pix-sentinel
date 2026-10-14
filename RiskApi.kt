package com.pixsentinel.api

import com.pixsentinel.domain.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@SpringBootApplication
class PixSentinelApplication {
    @Bean
    fun riskEngine() = RiskEngine()
}

fun main(args: Array<String>) {
    runApplication<PixSentinelApplication>(*args)
}

data class EvaluateRiskRequest(
    val transactionId: String,
    val accountId: String,
    val amount: BigDecimal,
    val currency: String = "BRL",
    val accountAgeDays: Int,
    val devicePreviouslySeen: Boolean,
    val transactionsLast10Minutes: Int,
    val distanceFromUsualLocationKm: Int
)

@RestController
@RequestMapping("/v1/risk")
class RiskController(private val engine: RiskEngine) {
    @PostMapping("/evaluate")
    fun evaluate(@RequestBody request: EvaluateRiskRequest): RiskDecision = engine.evaluate(
        PixTransaction(
            request.transactionId, request.accountId, request.amount, request.currency,
            request.accountAgeDays, request.devicePreviouslySeen,
            request.transactionsLast10Minutes, request.distanceFromUsualLocationKm
        )
    )
}
