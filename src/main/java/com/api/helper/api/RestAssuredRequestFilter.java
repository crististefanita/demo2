package com.api.helper.api;

import io.cucumber.java.Scenario;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.extern.log4j.Log4j;

@Log4j
public class RestAssuredRequestFilter implements Filter {

    private Scenario scenario;

    public RestAssuredRequestFilter(Scenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);
        if (scenario != null) {
            logScenarioInfo(requestSpec, response);
        } else {
            logConsoleInfo(requestSpec, response);
        }
        return response;
    }

    private void logScenarioInfo(FilterableRequestSpecification requestSpec, Response response) {
        scenario.log("---");
        scenario.log("Execute request: " + requestSpec.getMethod());
        scenario.log("URI: " + requestSpec.getURI());

        //scenario.log("Request Header: \n" + requestSpec.getHeaders());

        scenario.log("Body: " + requestSpec.getBody());
        scenario.log("Status: " + response.getStatusLine());

        //scenario.log("Response Header: \n"+ response.getHeaders());

        scenario.log("Response Body => " + response.getBody().asString());
    }

    private void logConsoleInfo(FilterableRequestSpecification requestSpec, Response response) {
        log.info("---");
        log.info("Execute request: " + requestSpec.getMethod());
        log.info("URI: " + requestSpec.getURI());

        //log.info("Request Header: \n" + requestSpec.getHeaders());

        log.info("Body: " + requestSpec.getBody());
        log.info("Status: " + response.getStatusLine());

        //log.info("Response Header: \n"+ response.getHeaders());

        log.info("Response Body => " + response.getBody().asString());
    }

}