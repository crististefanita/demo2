@UI
Feature: Title of your feature

#  @demo
#  Scenario: Title of your scenario
#    When I visit the homepage
#    And I click on login link

  @Sanity
  @GoogleTest
  Scenario: An user search after something in google
    Given an user opens "https://www.google.com/"
    Then the page title opened is "Google"
    When user search by "test"

