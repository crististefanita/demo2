package atf.ui.tests.autoex.api;

import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.testkit.AtfConsoleMessages;
import atf.api.testkit.AtfIntegrationTest;
import atf.api.testkit.channel.AtfTestChannel;
import atf.api.testkit.channel.TestChannel;
import atf.api.testkit.reporting.AtfReportPath;
import atf.ui.autoex.api.AutoExApiJsonField;
import atf.ui.autoex.api.AutoExApiRequestFactory;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/** Read-only public API sample; safe to run without creating or changing accounts. */
@AtfIntegrationTest
@AtfTestChannel(TestChannel.API)
@EnabledIfSystemProperty(named = "atf.autoex.api.readonly.live.enabled", matches = "true")
@AtfConsoleMessages(AtfConsoleMessages.Policy.DETAILS)
@AtfReportPath
@ExtendWith({AtfConsoleTestExtension.class, AutoExApiContractReportExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
public class AutoExApiReadOnlyLiveContractTest {
    @Test
    void readOnlyPublishedEndpointsProduceLiveExtentEvidence() {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();
        var api = AutoExApiLiveContractTest.liveExecutor(configuration);
        var requests = new AutoExApiRequestFactory();

        AutoExApiLiveContractTest.assertResponse("API 1 products list", api,
                requests.productsList(), 200, null, AutoExApiJsonField.PRODUCTS);
        AutoExApiLiveContractTest.assertResponse("API 3 brands list", api,
                requests.brandsList(), 200, null, AutoExApiJsonField.BRANDS);
        AutoExApiLiveContractTest.assertSearchResponse("API 5 product search", api,
                requests.searchProduct(configuration.catalog().searchTerm()), 200,
                configuration.catalog().searchTerm());
    }
}
