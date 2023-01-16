@UI
Feature: Title of your feature

  @Sanity
  @GoogleTest
  Scenario: An user search after something in google
    Given an user opens "https://www.google.com/"
    Then the page title opened is "Google"
    When user search by "test"

