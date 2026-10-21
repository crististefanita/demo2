package atf.ui.practiceshop.config;

/**
 * Stable API evidence seams in the Practice Shop journey.
 *
 * <p>The enum is intentionally consumer-owned: the reporting library knows
 * how to render a checkpoint, while this module decides which business seam
 * owns an exchange.</p>
 */
public enum PracticeShopApiCheckpoint {
    PRODUCT("product"),
    LOGIN("login"),
    CART("cart"),
    PAYMENT("payment"),
    ORDER("order");

    private final String key;

    PracticeShopApiCheckpoint(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static PracticeShopApiCheckpoint from(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        for (PracticeShopApiCheckpoint checkpoint : values()) {
            if (checkpoint.key.equals(normalized) || checkpoint.name().toLowerCase().equals(normalized)) {
                return checkpoint;
            }
        }
        throw new IllegalArgumentException("Unsupported Practice Shop API evidence checkpoint: " + value
                + ". Expected product, login, cart, payment or order");
    }
}
