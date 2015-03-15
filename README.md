Based on the assumption that the most stable part of the web application to be tested is the human readable semantic elements. Write test scripts as if describing to a user (over the phone or in documentation) what to click, type etc. So your tests should not need altering if you move your HTML around or change to/from links to buttons etc.
```java
robot.click("create user");
robot.set("Name", "Sam");
robot.click("Gender", "male");
robot.click("save");

robot.back();

assertEquals("Sam", robot.get("Name"));
assertTrue(robot.isSelected("Geek", "yes"));
```
Use
```xml
<dependency>
    <groupId>com.redspr.redrobot</groupId>
    <artifactId>redrobot-core</artifactId>
    <version>0.2.13</version>
</dependency>
```
##Theory
Your tests should only depend on the very simple com.redspr.redrobot.Robot interface which is based on the conversation you would have with your most stupid user. A simple algorithm is used to find the best match element based on one or more matching strings.

##Supported semantics
Markup | Example |	Top matches	| Notes
---------|-------|--------------|-------
for="id" |		   |              |
&lt;th>foo&lt;/th> |	robot.click("red", "two", "foo");	| ![](https://github.com/salk31/RedRobot/blob/master/images/tableRedTwoFoo.png) (1)	 |Gives the related columns a boost.
colspan="2" |	robot.click("bird", "three", "Foo"); | ![](https://github.com/salk31/RedRobot/blob/master/images/tableColSpanBirdThree.png)	 (1)	 |
role="button"	|		| |
role="listitem" | | |
<option selected>foo</option>	 | | 		Gives the selected option an extra boost.
(1) Debug output uses the following scale (from hot to cold) 
![](https://github.com/salk31/RedRobot/blob/master/images/scale.png)

##Implementation
Behind the simple Robot interface an implementation using WebDriver? is provided. There is legacy support for direct HtmlUnit? and Selenium 1 but this is being phased out.
