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
        Objects.requireNonNull(snapshot, "snapshot");
        target.detail(ApiEvidenceField.AUTH_PROFILE.label(), snapshot.authProfile());
        target.detail(ApiEvidenceField.AUTH_TYPE.label(), snapshot.authMethod().wireValue());
    }
}
