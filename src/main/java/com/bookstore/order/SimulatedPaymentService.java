package com.bookstore.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated payment processor — for demonstration only (FR-06).
 *
 * IMPORTANT:
 * - No real payment processor is contacted.
 * - No card numbers, CVVs, expiry dates, or banking credentials are accepted or stored.
 * - This class only receives the amount and payment method and returns a result
 *   containing a generated reference code.
 *
 * The simulation approves all well-formed requests. A hook is provided for a
 * deterministic decline path (negative/zero amount) so the failure branch
 * (HTTP 422 PAYMENT_DECLINED) can be exercised during the demo.
 */
@Service
public class SimulatedPaymentService {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentService.class);

    /**
     * Result of a simulated payment attempt.
     *
     * @param approved  whether the payment was approved
     * @param reference generated reference code (null when declined); never a real token
     */
    public record PaymentResult(boolean approved, String reference) {}

    /**
     * Simulates charging the given amount using the given method.
     *
     * @param amount        the order total
     * @param paymentMethod CREDIT_CARD or DEBIT_CARD
     * @return a PaymentResult with an approval flag and a generated reference on success
     */
    public PaymentResult charge(BigDecimal amount, PaymentMethod paymentMethod) {
        // Deterministic decline for non-positive amounts (defensive; also demoable).
        if (amount == null || amount.signum() <= 0) {
            log.debug("Simulated payment declined: non-positive amount {}", amount);
            return new PaymentResult(false, null);
        }

        String reference = "SIM-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        log.debug("Simulated payment approved via {} for amount {} -> ref {}",
                paymentMethod, amount, reference);
        return new PaymentResult(true, reference);
    }
}
