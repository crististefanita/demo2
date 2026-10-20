package atf.ui.practiceshop.api;

import java.util.Locale;

/** Supported payment variants exposed by the checkout screen. */
public enum PracticeShopPaymentMethod {
    BANK_TRANSFER("bank-transfer"),
    CASH_ON_DELIVERY("cash-on-delivery"),
    CREDIT_CARD("credit-card"),
    BUY_NOW_PAY_LATER("buy-now-pay-later"),
    GIFT_CARD("gift-card");

    private final String wireValue;

    PracticeShopPaymentMethod(String wireValue) { this.wireValue = wireValue; }

    public String wireValue() { return wireValue; }

    public static PracticeShopPaymentMethod from(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replace(' ', '-');
        for (PracticeShopPaymentMethod method : values()) {
            if (method.wireValue.equals(normalized)) return method;
        }
        throw new IllegalArgumentException("Unsupported payment method: " + value);
    }
}
