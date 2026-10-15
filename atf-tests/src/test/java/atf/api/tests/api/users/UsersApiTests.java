package atf.api.tests.api.users;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.tests.support.ApiResourceTestBase;
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
        var decision = validate("GET /public/v2/users — collection contract", execution,
                ContractValidators.userCollection());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().body().contains("\"id\":1")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void getsUserByIdAndValidatesTheItemContract() {
        var execution = fixture().api().users().get(1);
        var decision = validate("GET /public/v2/users/1 — item contract", execution,
                ContractValidators.userItem());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().url().endsWith("/public/v2/users/1")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void rejectsCreateWithoutAuthentication() {
        var execution = fixture().api().users().create(
                new atf.api.execution.model.UserRequest("ATF user", "atf@example.test", "male", "active"));
        var decision = validate("POST /public/v2/users — missing authentication", execution,
                new ResponseContract().status(401)
                        .contentTypeContains("application/json")
                        .bodyContains("Authentication required"));

        assertAll(
                () -> assertEquals(401, execution.response().status()),
                () -> assertTrue(execution.response().body().contains("Authentication required")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void keepsAContractMismatchAsWarningWhenWarningModeIsExplicit() {
        var execution = fixture().api().users().getCollection();
        var decision = validate("GET /public/v2/users — soft contract warning", execution,
                new ResponseContract().status(201).contentTypeContains("application/json"), true);

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertEquals(AtfReportStatus.WARNING, decision.status()),
                () -> assertFalse(decision.shouldThrow()));
    }
}
