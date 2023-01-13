package com.stepdef.basic;

import com.api.context.TestContext;
import com.api.helper.AssertTest;
import com.api.helper.api.RestAssuredRequestFilter;
import com.api.helper.file.PropertiesFile;
import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import com.github.dzieciou.testing.curl.Options;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.slf4j.event.Level;

public class BasicTestApi {

    protected Scenario scenario;
    protected TestContext context;
    protected AssertTest assertTest;

    public BasicTestApi(TestContext context) {
        this.context = context;
        assertTest = new AssertTest(context.scenario);
    }

    private static final String CONTENT_TYPE = PropertiesFile.getProperty("content.type");

    public RequestSpecification requestSetup() {
        RestAssured.reset();

        Options options = Options.builder().useLogLevel(Level.INFO).build();
        RestAssuredConfig config = CurlRestAssuredConfigFactory.createConfig(options);

        RestAssured.baseURI = PropertiesFile.getProperty("baseURL");

        return RestAssured.given()
                .config(config)
                .filter(new RestAssuredRequestFilter(scenario))
                .contentType(CONTENT_TYPE)
                .accept(CONTENT_TYPE);
    }

}
