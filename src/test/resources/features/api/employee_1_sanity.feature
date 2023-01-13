@Regression @Api
Feature: Sanity tests for verification of the api working fine

  @Sanity
  @EmployeeDetails
  Scenario: Employee details for a existing id
    Then  details of employee with id "5" and status code 200 is received

  @Sanity
  @EmployeeDetails
  Scenario: Employee details api with no id passed
    Given  an employee record is not added
    Then  status code 404 is received

