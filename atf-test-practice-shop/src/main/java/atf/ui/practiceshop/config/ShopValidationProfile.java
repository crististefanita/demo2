package atf.ui.practiceshop.config;

/** Selects the test pyramid profile without a boolean switch matrix. */
public enum ShopValidationProfile {
    UI_ONLY,
    UI_API;

    public static ShopValidationProfile from(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase();
        return switch (normalized) {
            case "ui-only", "ui_only", "ui" -> UI_ONLY;
            case "ui-api", "ui_api", "api", "" -> UI_API;
            default -> throw new IllegalArgumentException(
                    "Unsupported Practice Shop validation profile: " + value
                            + ". Expected ui-only or ui-api");
        };
    }

    public boolean apiEvidenceRequired() {
        return this == UI_API;
    }
}
