package com.pixsentinel.domain

import java.math.BigDecimal

enum class Decision { APPROVE, REVIEW, BLOCK }

data class PixTransaction(
    val transactionId: String,
    val accountId: String,
    val amount: BigDecimal,
    val currency: String,
    val accountAgeDays: Int,
    val devicePreviouslySeen: Boolean,
    val transactionsLast10Minutes: Int,
    val distanceFromUsualLocationKm: Int
)

data class RiskDecision(
    val transactionId: String,
    val decision: Decision,
    val riskScore: Int,
    val reasons: List<String>
)

class RiskEngine(
    private val amountLimit: BigDecimal = BigDecimal("3000.00"),
    private val velocityLimit: Int = 8,
    private val unusualDistanceKm: Int = 100
) {
    fun evaluate(tx: PixTransaction): RiskDecision {
        require(tx.amount > BigDecimal.ZERO) { "amount must be positive" }
        require(tx.currency == "BRL") { "only BRL is supported" }

        val reasons = buildList {
            if (tx.amount > amountLimit) add("AMOUNT_ABOVE_LIMIT")
            if (tx.accountAgeDays < 30) add("NEW_ACCOUNT")
            if (!tx.devicePreviouslySeen) add("NEW_DEVICE")
            if (tx.transactionsLast10Minutes > velocityLimit) add("HIGH_TRANSACTION_VELOCITY")
            if (tx.distanceFromUsualLocationKm > unusualDistanceKm) add("UNUSUAL_LOCATION")
        }

        val score = (reasons.size * 25).coerceAtMost(100)
        val decision = when {
            score >= 75 -> Decision.BLOCK
            score >= 50 -> Decision.REVIEW
            else -> Decision.APPROVE
        }
        return RiskDecision(tx.transactionId, decision, score, reasons)
    }
}
