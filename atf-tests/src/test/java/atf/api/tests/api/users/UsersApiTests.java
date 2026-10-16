package atf.api.tests.api.users;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.http.HttpMethod;
import atf.api.core.http.MediaTypes;
import atf.api.execution.routing.RoutePaths;
import atf.api.tests.support.junit.ApiResourceTestBase;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Readable user-resource API tests: action, response contract and verdict. */
class UsersApiTests extends ApiResourceTestBase {

    @Test
    void getsUserCollectionAndValidatesItsContract() {
        var execution = fixture().api().users().getCollection();
        var decision = validate(operation(HttpMethod.GET, RoutePaths.USERS, "collection contract"), execution,
                ContractValidators.userCollection());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().body().contains("\"id\":1")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void getsUserByIdAndValidatesTheItemContract() {
        var execution = fixture().api().users().get(1);
        var decision = validate(operation(HttpMethod.GET, RoutePaths.USERS + "/1", "item contract"), execution,
                ContractValidators.userItem());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().url().endsWith(RoutePaths.USERS + "/1")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void rejectsCreateWithoutAuthentication() {
        var execution = fixture().api().users().create(
                new atf.api.execution.model.UserRequest("ATF user", "atf@example.test", "male", "active"));
        var decision = validate(operation(HttpMethod.POST, RoutePaths.USERS, "missing authentication"), execution,
                new ResponseContract().status(401)
                        .contentTypeContains(MediaTypes.APPLICATION_JSON)
                        .bodyContains("Authentication required"));

        assertAll(
                () -> assertEquals(401, execution.response().status()),
                () -> assertTrue(execution.response().body().contains("Authentication required")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void keepsAContractMismatchAsWarningWhenWarningModeIsExplicit() {
        var execution = fixture().api().users().getCollection();
        var decision = validate(operation(HttpMethod.GET, RoutePaths.USERS, "soft contract warning"), execution,
                new ResponseContract().status(201).contentTypeContains(MediaTypes.APPLICATION_JSON), true);

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertEquals(AtfReportStatus.WARNING, decision.status()),
                () -> assertFalse(decision.shouldThrow()));
    }
}
