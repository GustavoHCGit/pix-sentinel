package com.pixsentinel.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RiskEngineTest {
    private val engine = RiskEngine()

    @Test
    fun `low risk transaction is approved`() {
        val result = engine.evaluate(tx(amount = "120.00", age = 900, seen = true, velocity = 1, distance = 2))
        assertEquals(Decision.APPROVE, result.decision)
        assertEquals(0, result.riskScore)
    }

    @Test
    fun `medium risk transaction goes to manual review`() {
        val result = engine.evaluate(tx(amount = "4500.00", age = 120, seen = true, velocity = 2, distance = 180))
        assertEquals(Decision.REVIEW, result.decision)
        assertTrue("AMOUNT_ABOVE_LIMIT" in result.reasons)
    }

    @Test
    fun `multiple signals block transaction`() {
        val result = engine.evaluate(tx(amount = "4500.00", age = 4, seen = false, velocity = 12, distance = 180))
        assertEquals(Decision.BLOCK, result.decision)
        assertEquals(100, result.riskScore)
        assertEquals(5, result.reasons.size)
    }

    @Test
    fun `invalid amount is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            engine.evaluate(tx(amount = "0.00"))
        }
    }

    @Test
    fun `non BRL transaction is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            engine.evaluate(tx(amount = "10.00").copy(currency = "USD"))
        }
    }

    private fun tx(
        amount: String,
        age: Int = 900,
        seen: Boolean = true,
        velocity: Int = 1,
        distance: Int = 2
    ) = PixTransaction("pix-1", "acc-1", BigDecimal(amount), "BRL", age, seen, velocity, distance)
}
