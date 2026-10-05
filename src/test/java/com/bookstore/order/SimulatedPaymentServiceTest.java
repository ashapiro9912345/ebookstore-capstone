package com.bookstore.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the simulated payment processor (FR-06).
 *
 * Verifies:
 *  - a well-formed positive amount is approved and returns a SIM-* reference
 *  - a non-positive amount is deterministically declined (the 422 PAYMENT_DECLINED path)
 *  - no card data is involved (the API only accepts amount + method)
 */
class SimulatedPaymentServiceTest {

    private final SimulatedPaymentService service = new SimulatedPaymentService();

    @Test
    @DisplayName("Simulated payment succeeds for a positive amount and returns a SIM reference")
    void approvesPositiveAmount() {
        SimulatedPaymentService.PaymentResult result =
                service.charge(new BigDecimal("85.00"), PaymentMethod.CREDIT_CARD);

        assertThat(result.approved()).isTrue();
        assertThat(result.reference())
                .isNotNull()
                .startsWith("SIM-")
                .hasSize(12); // "SIM-" + 8 hex chars
    }

    @Test
    @DisplayName("Simulated payment is approved for DEBIT_CARD too")
    void approvesDebitCard() {
        SimulatedPaymentService.PaymentResult result =
                service.charge(new BigDecimal("10.50"), PaymentMethod.DEBIT_CARD);

        assertThat(result.approved()).isTrue();
        assertThat(result.reference()).startsWith("SIM-");
    }

    @Test
    @DisplayName("Simulated payment is declined for a zero amount (demoable failure path)")
    void declinesZeroAmount() {
        SimulatedPaymentService.PaymentResult result =
                service.charge(BigDecimal.ZERO, PaymentMethod.CREDIT_CARD);

        assertThat(result.approved()).isFalse();
        assertThat(result.reference()).isNull();
    }

    @Test
    @DisplayName("Simulated payment is declined for a negative amount")
    void declinesNegativeAmount() {
        SimulatedPaymentService.PaymentResult result =
                service.charge(new BigDecimal("-1.00"), PaymentMethod.CREDIT_CARD);

        assertThat(result.approved()).isFalse();
        assertThat(result.reference()).isNull();
    }

    @Test
    @DisplayName("Simulated payment is declined for a null amount")
    void declinesNullAmount() {
        SimulatedPaymentService.PaymentResult result =
                service.charge(null, PaymentMethod.CREDIT_CARD);

        assertThat(result.approved()).isFalse();
        assertThat(result.reference()).isNull();
    }
}
