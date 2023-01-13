# Cucumber-with-Selenium-in-Java
Sample project on how to use Cucumber with Selenium in Java along with Junit

##What is Cucumber?

Cucumber is a testing framework which supports Behavior Driven Development (BDD). It lets us define application behavior in plain meaningful English text using a simple grammar defined by a language called Gherkin. Cucumber itself is written in Ruby, but it can be used to “test” code written in Ruby or other languages including but not limited to Java, C# and Python.

Cucumber adds an extra layer of abstraction to our testing paradigm. This layer of abstration is the highest abstraction there is - the English language. Cucucmber converts a feature written in English to executable code, enabling every person, regardless of their coding expertise, to understand exactly what the feature tests and the expected output.

In this framework, I have used "Cucumber-Jvm", a Cucumber implementation for **Java** along with **JUnit** and open source testing framework **Selenium (WebDriver)**. 

##Cucumner basics

As we mentioned, Cucumber converts features written in English to executable code. So, a feature can be thought of as any stateful "black box" in your application that receives input and generates output. Given the "black box" has a state, for each input, we expect to see a specfic output.

Before starting to code, it is considered a good practice to write down the entire feature file. This gives us a general outline of how the code behind the feature should look like.

##Let's get started


A full Cucumber flow is comprised of 3 components: **feature file**, **steps file**, and **runner file**.

##Feature file:
An example of a feature file with one scenario looks like this:
```
Feature: Namely New Functionality
Background:
Given I Browse to the application
When User login as a valid user
Then Namely home page should display
    
Scenario: 1. Logout after successfull login
When User logout from the application
Then Namely login page should show
```
Given, When, and Then are keywords in Gherkin, Cucumber's plain text DSL.

##Steps file:

A Step Definition is a small piece of code with a pattern attached to it or in other words a Step Definition is a java method in a class with an annotation above it. An annotation followed by the pattern is used to link the Step Definition to all the matching Steps, and the code is what Cucumber will execute when it sees a Gherkin Step. Cucumber finds the Step Definition file with the help of Glue code in Cucumber Options. 

Cucumber knows how to glue these English sentences to java methods using regexes. The file used to run these sentences is called a "Steps" files, because it runs the feature "step by step". Each step in the feature is matched against a regex, owned by a specific method in the Steps file. Arguments can be passed to the method via the regex.

An example of a Steps file matching the above Given statement of the ".feature" file looks like this:
```
@Given("^I Browse to the application$")
public void browse_to_the_application() throws Throwable {
  String url = environment.getProperty("root.url");
	driver.navigate().to(url);
	}
```

##Runner file:

The last member when running a cucumber test is the "JUnit-Runner" class, an empty Junit class that is used as a boot class.

An example of a runner class looks like this:
```
@RunWith(Cucumber.class)
@CucumberOptions(
		  dryRun = false,
		  strict = true,
		  monochrome = true,
		  tags = { "@payroll1" },
		  features = "src/test/resources/features",
		  glue = {"com.namely.automation.framework.hook","com.namely.automation.cucumber.steps"},
		  plugin={
			 "pretty",
			 "html:target/test-report",
			 "json:target/test-report.json",
			 "json:target/cucumber-report.json",
			 "junit:target/test-report.xml"
		  	}
		)
  public class NamelyCocumberRunner {

  }
```


Here, we tell JUnit to run the test using Cucumber **- @RunWith(Cucumber.class)**, and tell **"Cucumber-Jvm"** which configurations to run **- @CucucmberOptions**. 

##What is Cucumber Options ?
In layman language @CucumberOptions are like property file or settings for your test. Basically @CucumberOptions enables us to do all the things that we could have done if we have used cucumber command line. 

##Dry Run
dryRun option can either set as true or false. If it is set as true, it means that Cucumber will only checks that every Step mentioned in the Feature File have corresponding code written in Step Definition file or not. So in case any of the function is missed in the Step Definition for any Step in Feature File, it will give us the message. 

##Monochrome
This option can either set as true or false. If it is set as true, it means that the console output for the Cucumber test are much more readable. And if it is set as false, then the console output is not as readable as it should be.

##Features
Features Options helps Cucumber to locate the Feature file in the project folder structure

##Glue
It is almost the same think as Features Option but the only difference is that it helps Cucumber to locate the Step Definition file. Whenever Cucumber encounters a Step, it looks for a Step Definition inside all the files present in the folder mentioned in Glue Option. It can be specified like:
```
glue = “stepDefinition“
```
Or if the Step Definition file is in the deep folder structure 
```
glue = “src/test/stepDeinition“
```
##Format

Format Option is used to specify different formatting options for the output reports. Various options that can be used as for-matters are:

**Pretty:** Prints the Gherkin source with additional colours and stack traces for errors. Use below code:
```
format = {“pretty“}
```
 
**HTML**: This will generate a HTML report at the location mentioned in the for-matter itself. Use below code:
```
format = {“html:Folder_Name“}
```
 
**JSON:** This report contains all the information from the gherkin source in JSON Format. This report is meant to be post-processed into another visual format by 3rd party tools such as Cucumber Jenkins. Use the below code:
```
format = {“json:Folder_Name/cucumber.json“}
```
**JUnit:** This report generates XML files just like Apache Ant’s JUnit report task. This XML format is understood by most Continuous Integration servers, who will use it to generate visual reports. use the below code:
```
format = { “junit:Folder_Name/cucumber.xml“}
```
The location of the ".feature" file as well as the location of the directory containing the Steps file (glue) is mandatory. 


Any Question? just shoot!
	 

