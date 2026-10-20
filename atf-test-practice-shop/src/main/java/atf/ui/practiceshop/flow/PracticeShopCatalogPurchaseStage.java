package atf.ui.practiceshop.flow;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.ui.practiceshop.pages.PracticeShopHomePage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;

import java.util.List;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** Catalog-side business stage: home, product selection and add-to-cart. */
final class PracticeShopCatalogPurchaseStage {
    void prepareCart(PracticeShopPurchaseJourneyContext context) {
        var page = context.page();
        var actions = context.actions();
        var locators = context.locators();
        var configuration = context.configuration();

        PracticeShopHomePage home = new PracticeShopHomePage(page, locators, actions);
        home.open(configuration);
        context.assertVisible(locators.homeNavigation(page), locators.homeBrand(page),
                locators.homeBanner(page));
        context.publish(context.checkpoint(AtfGherkinKeyword.GIVEN,
                "catalog home structure displays navigation, branding and banner",
                context.homeScreenValidator().structure(page),
                context.homeScreenValidator().structureStyle(page), List.of()));

        context.assertVisible(locators.homeSort(page), locators.homePriceRange(page),
                locators.searchQuery(page), locators.homeSearchReset(page),
                locators.homeSearchSubmit(page), locators.homeCategoryFilters(page),
                locators.homeBrandFilters(page), locators.homeEcoFilter(page));
        context.publish(context.checkpoint(AtfGherkinKeyword.AND,
                "catalog home filters expose sorting, price, search and category controls",
                context.homeScreenValidator().filters(page),
                context.homeScreenValidator().filterIntegrity(page), List.of()));

        context.assertVisible(locators.homeProductGrid(page), locators.homeProductNames(page),
                locators.homeProductImages(page), locators.homePagination(page), locators.homeFooter(page));
        context.publish(context.checkpoint(AtfGherkinKeyword.AND,
                "catalog home renders product cards, pagination and footer",
                context.homeScreenValidator().productGrid(page),
                context.homeScreenValidator().productGridIntegrity(page), List.of()));

        home.search(configuration.productName());
        var productLink = configuration.usesDynamicProductPath()
                ? locators.productLinkFirst(page, configuration.productName())
                : locators.productLinkByPath(page, configuration.productPath());
        boolean configuredProductPathFound = configuration.usesDynamicProductPath()
                || productLink.locator().count() > 0;
        if (!configuredProductPathFound) {
            throw new IllegalStateException("Configured Practice Shop product route was not found: "
                    + configuration.productPath() + "; productName=" + configuration.productName());
        }
        String selectedProductPath = productLink.locator().getAttribute("href");
        if (selectedProductPath == null || selectedProductPath.isBlank()) {
            throw new IllegalStateException("Selected Practice Shop product has no href: "
                    + productLink.selector());
        }
        actions.waitForVisible(productLink);
        actions.click(productLink, "matching product opens from search results");

        PracticeShopProductPage product = new PracticeShopProductPage(page, locators, actions);
        String productName = product.assertLoaded(configuration);
        var productApiAssertions = context.apiNetwork().productReady(
                configuration.productName(), selectedProductPath);
        context.publish(context.checkpoint(AtfGherkinKeyword.GIVEN,
                "product page displays the configured product and ready-to-buy fields",
                context.pageValidator().validate(page, PracticeShopProductPage.class,
                        configuration.productName()),
                List.of(
                        check("Product name", configuration.productName(), productName,
                                productName.contains(configuration.productName())),
                        check("Configured product route", configuration.productPathExpectation(),
                                selectedProductPath,
                                configuredProductPathFound && configuration.matchesProductPath(selectedProductPath)),
                        check("Selected product route loaded", selectedProductPath, page.url(),
                                selectedProductPath != null && page.url().contains(selectedProductPath))),
                context.apiNetwork().productEvidence())
                .withAdditionalAssertions(productApiAssertions)
                .withAdditionalAssertions(context.purchaseScreenValidator().product(page)));

        product.addConfiguredQuantity(configuration);
    }
}
