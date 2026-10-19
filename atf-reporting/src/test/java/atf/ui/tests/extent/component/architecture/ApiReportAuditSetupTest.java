package atf.ui.tests.extent.component.architecture;
import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.extent.support.api.ApiReportAuditSetup;
import atf.ui.tests.extent.support.data.UiTestData;

import atf.api.ui.config.UiBrowserConfiguration;
import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.ui.tests.extent.support.api.ApiReportSetupDeclarations;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AtfComponentTest
class ApiReportAuditSetupTest {
    @Test
    void annotationOverridesTheBroaderYamlCollectionForOneUiArtifact() {
        UiConfiguration yamlFallback = UiConfiguration.defaults(Path.of("target/api-setup-test"));

        assertEquals(List.of("users", "posts"),
                ApiReportAuditSetup.families(ApiReportSetupDeclarations.UsersPosts.class, yamlFallback));
    }

    @Test
    void emptyAnnotationDelegatesToTheYamlCollection() {
        UiConfiguration yamlFallback = new UiConfiguration(
                UiBrowserConfiguration.defaults(), Path.of("target/api-setup-test"), "ui_",
                List.of("posts", "users"), UiScreenValidationConfiguration.defaults(), Optional.empty());

        assertEquals(List.of("posts", "users"),
                ApiReportAuditSetup.families(ApiReportSetupDeclarations.Empty.class, yamlFallback));
    }

    @Test
    void manualJvmCollectionOverridesAnnotationForAdHocReportValidation() {
        String previous = System.getProperty(UiTestData.API_FAMILIES_PROPERTY);
        try {
            System.setProperty(UiTestData.API_FAMILIES_PROPERTY,
                    UiTestData.REQUESTS_FAMILY + "," + UiTestData.HEADERS_FAMILY);
            assertEquals(List.of(UiTestData.REQUESTS_FAMILY, UiTestData.HEADERS_FAMILY),
                    ApiReportAuditSetup.families(ApiReportSetupDeclarations.UsersPosts.class,
                            UiConfiguration.defaults(Path.of("target/api-setup-test"))));
        } finally {
            if (previous == null) System.clearProperty(UiTestData.API_FAMILIES_PROPERTY);
            else System.setProperty(UiTestData.API_FAMILIES_PROPERTY, previous);
        }
    }
}
