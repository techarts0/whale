# whale: A JSR330 Based Java IOC Container

## 1. Summary
whale is a lightweight IOC Container that fulfills JSR330. Most of java developers are familiar with spring framework or google guice, we recommend you to try whale.

## 2. Basic Usage
Whale supports 3 ways to define the dependence of java classes.
### A. JSR 330 Annotation

```
package ioc.demo;

@Named
@Singleton
public class Person{
    @Inject 
    @Valued(val="3")
    private int id;
    
    @Inject
    @Valued(key="user.name") 
    private String name;
    
    @Inject
    private Mobile mobile;
    
    public Person(){}

    //Getters and Setters
}

package ioc.demo;

@Singleton
public class Mobile{
    private String areaCode;
    private String number;

    @Inject
    public Mobile(@Valued(key="mobile.area")String areaCode, @Valued(key="mobile.number")String number){
        this.areaCode = areaCode;
        this.number = number;
    }
    //Getters & Setters
}

public class JSR330Test{
    private static Map<String, String> configs = Map.of("user.name", "John", 
                                                        "mobile.area", "+86", 
                                                        "mobile.number", "13666666666");
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.register(Person.class).register(Mobile.class);
        //factory.register("ioc.demo.person").register("ioc.demo.Mobile");
        //factory.register(Person.class, Mobile.class);
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
}
```
The @Valued is a not a JSR330 official annotation, but it's very useful.
We suppose the above 2 classes is under the folder "/usr/project/demo/classes", the framework will scan the classpath to register beans:

```
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.scan("/usr/project/demo/classes");
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
```
Maybe, the classes in the JAR file demo.jar and it's full path is "/usr/project/demo/lib/demo.jar":

```
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.load("/usr/project/demo/lib/demo.jar");
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
```
### B. XML Definition(beans.xml)

If you are a spring-framework developer, the XML configuration is very familiar. Whale allows you define the manged beans in the XML file(beans.xml):
```
`<beans>
    <bean id="person" singleton="true" type="ico.demo.Person">
        <props>
	    <prop name="id" val="45" />
	    <prop name="name" key="user.name" />
            <prop name="mobile" ref="mobile" />
	</props>
    </bean>
    <bean id="mobile" singleton="true" type="ico.demo.Mobile">
        <args>
	    <arg key="mobile.area" type="String" />
	    <arg key="mobile.number" type="String" />
	</args>
    </bean>
</beans>` 
```
Please note, XML does not support method rejection, because the grammar is to complex and urgly :(
```
    @Test
    public void testInject(){
        var context = Context.make(configs);
        var factory = context.createFactory();
        factory.parse("/usr/project/demo/classes/beans.xml");
        factory.start();
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13666666666", person.getMobile().getNumber());
    }
```
Actually, you can pass multiple xml definitions to the method parse like the following:
```
    factory.parse("/usr/project/demo/classes/beans_1.xml", "/usr/project/demo/classes/beans_2.xml");
```

## 3. Usage of Provider<T>

The biggest benift of Provider interface is resolved the problem of circular dependent. For example:
```
// In class Person:
@Inject
private Mobile mobie;

//In class Mobile
@Inject
private Person owner;

```
The framework will throw an exception "Circular dependent is detected". We can rewrite it using Provider:
```
// In class Person:
@Inject
private Provider<Mobile> mobile;

//In class Mobile
@Inject
private Provider<Person> owner;

//In test case:
var code = person.getMobile().get().getAreaCode();
var name = mobile.getOwner().get().getName();
```
Now, it works correctly.

## 4. Web Application

The WebListener class provides the ability to integrate whale on a web application. Please add a listener tag in web.xml:

```
<listener>
    <listener-class>cn.techarts.xkit.ioc.WebListener</listener-class>
  </listener>
```
The IOC context is cached globally into servlet context. 
```
//Usage in servlet:
var servletContext = request.getServletContext();
var context = Conext.from(servletContext);
var person = context.get(Person.class);
var mobile = context.get(Mobile.class);
```

## 5. More Features
I am writting hard...