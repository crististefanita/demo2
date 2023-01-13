@Regression @Api
Feature: Regression tests for verification of the api working fine

  @AllEmployees
  Scenario: Employee details api
    Then  details of employees with status code 200 is received

  @DeleteEmployee
  Scenario: deleting an existing employee
    Given an employee record is added
    Then an employee id "6" is deleted and response status code 200 is returned


  @AddEmployee
  Scenario: adding a new employee
    Then a random employee is added and status code 200 is returned


  @UpdateEmployee
  Scenario: Updating a existing employee record
    Given an employee record is already added
    Then an employee with id "4" is updated and response status code 200 is returned
