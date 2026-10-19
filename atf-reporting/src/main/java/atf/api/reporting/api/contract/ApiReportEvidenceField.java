package atf.api.reporting.api.contract;

/** Stable field vocabulary emitted by the API evidence projection. */
public enum ApiReportEvidenceField {
    AUTH_PROFILE("AUTH PROFILE"),
    REQUEST_HEADERS("REQUEST HEADERS"),
    RESPONSE_HEADERS("RESPONSE HEADERS"),
    HTTP_STATUS("HTTP STATUS"),
    CONTENT_TYPE("CONTENT-TYPE"),
    RESPONSE_STATUS("response.status"),
    RESPONSE_URL("response.url");

    private final String key;

    ApiReportEvidenceField(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
