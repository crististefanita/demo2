package atf.ui.practiceshop.pages;

import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.config.PracticeShopRegistrationData;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.List;

/** Registration page object; account creation is explicit and never implicit in a purchase test. */
@PracticeShopPageContract(screen = PracticeShopScreen.REGISTRATION, route = PracticeShopUiRoutes.AUTH_REGISTER,
        requiredControls = {PracticeShopControl.REGISTER_FIRST_NAME, PracticeShopControl.REGISTER_LAST_NAME,
                PracticeShopControl.REGISTER_DOB, PracticeShopControl.REGISTER_COUNTRY,
                PracticeShopControl.REGISTER_POSTAL_CODE, PracticeShopControl.REGISTER_HOUSE_NUMBER,
                PracticeShopControl.REGISTER_STREET, PracticeShopControl.REGISTER_CITY,
                PracticeShopControl.REGISTER_STATE, PracticeShopControl.REGISTER_PHONE,
                PracticeShopControl.REGISTER_EMAIL, PracticeShopControl.REGISTER_PASSWORD,
                PracticeShopControl.REGISTER_SUBMIT}, bodyMarker = "Customer registration")
public final class PracticeShopRegistrationPage {
    private final Page page;
    private final PracticeShopLocators locators;
    private final PlaywrightActionSequence actions;

    public PracticeShopRegistrationPage(Page page, PracticeShopLocators locators,
                                        PlaywrightActionSequence actions) {
        this.page = page;
        this.locators = locators;
        this.actions = actions;
    }

    public void open(PracticeShopConfiguration configuration) {
        actions.navigate(page, configuration.uri(PracticeShopUiRoutes.AUTH_REGISTER), "registration page is loaded");
        actions.waitForText(page, locators.pageBody(page), "Customer registration",
                "registration form is displayed");
    }

    public void fill(PracticeShopRegistrationData data) {
        actions.fill(locators.registerFirstName(page), data.firstName(), "registration first name is entered");
        actions.fill(locators.registerLastName(page), data.lastName(), "registration last name is entered");
        actions.fill(locators.registerDob(page), data.dateOfBirth(), "registration date of birth is entered");
        actions.waitForVisible(locators.registerCountry(page));
        actions.selectOption(locators.registerCountry(page), data.country(), "registration country is selected");
        actions.fill(locators.registerPostalCode(page), data.postalCode(), "registration postal code is entered");
        actions.fill(locators.registerHouseNumber(page), data.houseNumber(), "registration house number is entered");
        page.waitForFunction("""
                selectors => selectors.every(selector => {
                    const field = document.querySelector(selector);
                    return field && field.value.trim().length > 0;
                })
                """, List.of(locators.registerStreet(page).selector(),
                locators.registerCity(page).selector(), locators.registerState(page).selector()));
        actions.observation("Address lookup", "registration address fields",
                "the shop fills street, city and state after country, postal code and house number are entered",
                "street, city and state were populated before configured values were applied");
        actions.fill(locators.registerStreet(page), data.street(), "registration street is entered");
        actions.fill(locators.registerCity(page), data.city(), "registration city is entered");
        actions.fill(locators.registerState(page), data.state(), "registration state is entered");
        actions.fill(locators.registerPhone(page), data.phone(), "registration phone is entered");
        actions.fill(locators.registerEmail(page), data.email(), "registration email is entered");
        actions.fill(locators.registerPassword(page), data.password(), "registration password is entered securely");
    }

    public void submit() {
        actions.click(locators.registerSubmit(page), "registration form is submitted");
    }

    /**
     * Submits once and waits for the API response from the configured host.
     * There is deliberately no retry: a timeout leaves the external outcome
     * uncertain and the caller must stop rather than replay account creation.
     */
    public int submitAndAwaitApiResponse(URI apiEndpoint) {
        URI endpoint = java.util.Objects.requireNonNull(apiEndpoint, "apiEndpoint");
        var response = page.waitForResponse(candidate -> isApiPost(candidate, endpoint),
                () -> actions.click(locators.registerSubmit(page), "registration form is submitted once"));
        int status = response.status();
        if (status < 200 || status >= 300) {
            throw new AssertionError("Practice Shop registration response was not successful; HTTP " + status);
        }
        return status;
    }

    static boolean isApiPost(com.microsoft.playwright.Response response, URI apiEndpoint) {
        if (response == null || response.request() == null || apiEndpoint == null) return false;
        if (!"POST".equalsIgnoreCase(response.request().method())) return false;
        try {
            return apiEndpoint.getHost().equalsIgnoreCase(URI.create(response.url()).getHost());
        } catch (RuntimeException malformedUrl) {
            return false;
        }
    }
}
