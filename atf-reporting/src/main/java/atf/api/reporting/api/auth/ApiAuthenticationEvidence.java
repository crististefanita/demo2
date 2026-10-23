package atf.api.reporting.api.auth;

import atf.api.reporting.api.ApiEvidenceField;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.contract.AtfReportPorts;

import java.util.Objects;

/** One ordered auth schema for direct API, audit and nested UI/API requests. */
public final class ApiAuthenticationEvidence {
    private ApiAuthenticationEvidence() { }

    public static void publishTo(AtfReportPorts.Request target, ReportRequest snapshot) {
        Objects.requireNonNull(target, "target");
        publishTo(target::detail, snapshot);
    }

    public static void publishTo(java.util.function.BiConsumer<String, String> detail, ReportRequest snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        detail.accept(ApiEvidenceField.AUTH_PROFILE.label(), snapshot.authProfile());
        detail.accept(ApiEvidenceField.AUTH_TYPE.label(), snapshot.authMethod().wireValue());
    }
}
