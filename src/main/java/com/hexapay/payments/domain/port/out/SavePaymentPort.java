package com.hexapay.payments.domain.port.out;

import com.hexapay.payments.domain.model.Payment;

/**
 * Driven port — SavePayment.
 *
 * This interface is OWNED by the domain, implemented by the persistence adapter.
 * The domain dictates the contract; infrastructure conforms to it — not the other way around.
 */
public interface SavePaymentPort {

    /**
     * Persists (inserts or updates) the given payment and returns its saved state.
     *
     * @param payment the payment aggregate to persist
     * @return the persisted payment (may include DB-generated values)
     */
    Payment save(Payment payment);
}
