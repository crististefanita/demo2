package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfComponentTest;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Fast shop component, support-contract and architecture checks; no live purchase journeys. */
@Suite
@SelectPackages({
        "atf.ui.tests.practiceshop.component",
        "atf.ui.tests.practiceshop.api.component",
        "atf.ui.tests.practiceshop.api.reporting",
        "atf.ui.tests.practiceshop.api.network",
        "atf.ui.tests.practiceshop.support"
})
@AtfComponentTest
public class ShopComponentSuite { }
