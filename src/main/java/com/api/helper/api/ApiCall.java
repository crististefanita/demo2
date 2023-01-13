package com.api.helper.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ApiCall {

    public static Response put(RequestSpecification request, String url, String body) {
        return request.body(body).when().put(url);
    }

    public static Response post(RequestSpecification request, String url, String body) {
        return request.body(body).when().post(url);
    }

    public static Response get(RequestSpecification request, String url) {
        return request.when().get(url);
    }

}
