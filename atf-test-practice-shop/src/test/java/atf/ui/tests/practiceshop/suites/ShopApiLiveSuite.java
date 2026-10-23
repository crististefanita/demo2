package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfIntegrationTest;
import atf.ui.tests.practiceshop.api.integration.ShopStandaloneApiReportTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Real HTTP contract gate; the selected class retains its explicit live opt-in. */
@Suite
@SelectClasses(ShopStandaloneApiReportTest.class)
@AtfIntegrationTest
public class ShopApiLiveSuite { }
