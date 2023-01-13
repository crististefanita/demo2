package com.api.context;

import com.api.helper.api.RestAssuredRequestFilter;
import com.api.helper.file.PropertiesFile;
import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import com.github.dzieciou.testing.curl.Options;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    public Map<String, Object> session = new HashMap<>();

    public Scenario scenario;

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
