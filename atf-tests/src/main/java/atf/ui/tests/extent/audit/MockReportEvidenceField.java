package atf.ui.tests.extent.audit;

/** Canonical keys emitted by the mock-backed API evidence projection. */
enum MockReportEvidenceField {
    AUTH_PROFILE("AUTH PROFILE"),
    REQUEST_HEADERS("REQUEST HEADERS"),
    RESPONSE_HEADERS("RESPONSE HEADERS"),
    HTTP_STATUS("HTTP STATUS"),
    CONTENT_TYPE("CONTENT-TYPE"),
    RESPONSE_STATUS("response.status"),
    RESPONSE_URL("response.url");

    private final String key;

    MockReportEvidenceField(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}
