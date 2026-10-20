package atf.ui.practiceshop.config;

import java.util.Objects;

/** Typed, YAML-driven purchase inputs; no checkout value is hidden in a flow. */
public record PracticeShopCheckoutConfiguration(
        int quantity,
        PracticeShopGuest guest,
        PracticeShopBillingAddress billing,
        String paymentMethod) {

    public PracticeShopCheckoutConfiguration {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be positive");
        guest = Objects.requireNonNull(guest, "guest");
        billing = Objects.requireNonNull(billing, "billing");
        paymentMethod = required(paymentMethod, "paymentMethod");
    }

    static PracticeShopCheckoutConfiguration defaults() {
        return new PracticeShopCheckoutConfiguration(2,
                new PracticeShopGuest("atf.practice.shop@example.test", "ATF", "PracticeShop"),
                new PracticeShopBillingAddress("Romania", "010101", "10", "Test Street",
                        "Bucharest", "Bucharest"),
                "Cash on Delivery");
    }

    private static String required(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }
}
