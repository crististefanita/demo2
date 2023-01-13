package com.api.helper;

import io.cucumber.java.Scenario;
import lombok.extern.log4j.Log4j;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;


@Log4j
public class AssertTest {

    private static Scenario scenario;

    public AssertTest(Scenario scenario) {
        this.scenario = scenario;
    }

    public static <T> void assertTest(String reason, T actual, Matcher<? super T> matcher) {
        Description description = new StringDescription();
        description.appendText(reason).appendText(" - \nExpected: ").appendDescriptionOf(matcher).appendText("\n  found: ");
        matcher.describeMismatch(actual, description);

        scenario.log(description.toString());
        log.info(description);

        if (!matcher.matches(actual)) {
            throw new AssertionError(description.toString());
        }
    }


}
