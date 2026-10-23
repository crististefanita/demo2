package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfUnitTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Runs deterministic Practice Shop API rules without constructing a client or network transport. */
@Suite
@SelectPackages({
        "atf.ui.tests.practiceshop.api.unit",
        "atf.ui.tests.practiceshop.api.reporting"
})
@IncludeTags("unit")
@AtfUnitTest
public class ShopApiUnitSuite { }
