package com.api.model;

import com.github.javafaker.Faker;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Employee {
    String name;
    int salary;
    int age;

    public static Employee randomBuilder() {
        Faker faker = new Faker();

        return Employee.builder()
                .name(faker.name().fullName())
                .salary(faker.number().numberBetween(100, 999999))
                .age(faker.number().numberBetween(25, 80))
                .build();
    }
}
