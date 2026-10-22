package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfComponentTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Runs request-executing API components and aggregates their observed exchanges into one Extent report. */
@Suite
@SelectPackages("atf.ui.tests.practiceshop.api.component")
@IncludeTags("api-call")
@AtfComponentTest
public class ShopApiCallAuditSuite { }
