# Whale: A JSR330 Based Java DI Framework

[![Generic badge](https://img.shields.io/badge/Active-00EE00.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/JAVA-8A2BE2.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Dependence_Injection-FFFF00.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/IOC-009ACD.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/JSR330-0000CD.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Java_EE_API-javax.inject-F4A460.svg)](https://shields.io/)
[![Generic badge](https://img.shields.io/badge/Jakarta_EE_API-jakarta.inject-FF4040.svg)](https://shields.io/)


## 1. Summary
Whale is a lightweight dependence Injection(DI) container that fully implements JSR330, and supports  **javax.inject**  and  **jakarta.inject**  API both. If you are a Java developer and familiar with spring framework or google guice, we highly recommend you giving whale a try.

## 2. Annotations

JSR330's appeal lies in its simplicity, consisting of just 4 annotations and one interface. To enhance flexibility, whale adds 2 annotations: Valued and Bind, as follows:

| #    | Annotation | Usage                                                        |
| ---- | ---------- | ------------------------------------------------------------ |
| 1    | Inject     | Indicates a field, constructor or method that will be injected with a managed object. |
| 2    | Named      | Gives the managed object a qualifier name, or tells the injector which will be injected in. |
| 3    | Valued     | Injects a value(primitive types like int, string, boolean) or a key from configuration file. (Non JSR330) |
| 4    | Singleton  | There is only one instance of the managed object in DI container. |
| 5    | Bind       | Relates an interface or abstraction to a specific implementation. (Non JSR330) |
| 6    | Qualifier  | Meta annotation.                                             |
| 7    | Provider   | An interface, not an annotation, used for resolving circular dependencies or lazy loading. |

Whale supports three dependence types:

- **REF**: A managed object in container. XML property is ref;
- **KEY**: A configuration from properties file. XML property is key;
- **VAL**: A value of primitive type(e. g. int, String, boolean, float), XML property is val;


## 3. Basic Usage
Whale offers four approaches for managing the dependencies between Java objects. To illustrate these methods, let's examine some sample test code. We'll assume the test code is located in the directory on the classpath: "**/tmp/project/demo/bin**".

- The Class Person dependents on the class Mobile, a given number value and some configurations:


```java
package whale.demo;

@Named
@Singleton
public class Person{
    @Inject 
    @Valued(val="3")
    private int id;
    
    @Inject
    @Valued(key="user.name") 
    private String name;
    
    private int age;
    
    @Inject
    private Mobile mobile;
    
    public Person(){}

    @Inject
    public void setAge(@Valued(key="user.age")int age){
        this.age = age;
    }
    
    //Getters and Setters
}
```
- The class Mobile dependents on two keys from configuration and is injected via the constructor:


```java
package whale.demo;

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
```

- To ensure successful testing, we need to prepare a configuration beforehand (/tmp/project/demo/config.properties):


```properties
user.age=18
user.name=John Denver
mobile.area=+86
mobile.number=13603166666
```

- or some static test data:


```java
public class TestData{
	public static final Map<String, String>
	CONFIGS = Map.of("user.age", "18",
					"user.name", "John Denver", 
             		 "mobile.area", "+86", 
             		 "mobile.number", "13603166666");
}
```

### A.  Scan classpath to resolve the dependencies:

The JUNIT test case as following:


```java
public class WhaleTest{
    @Test
    public void testScanClasspath(){
        var context = Context.make(CONFIGS);
        
        //Or read configuration from a properties file
        //var context = Context.make("/tmp/project/demo/config.properties");
        
        var factory = context.createFactory();
        factory.scan("/tmp/project/demo/bin");
        
        //If you have more than one classpath:
        //factory.scan("Another classpath");
        //factory.scan("classpath-1", "class-path2");
        
        factory.start();
        
        //The chain-stype calling is supported:
        //context.createFactory().scan("/tmp/project/demo/bin").start();
        
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals("John Denver", person.getName());
        TestCase.assertEquals(18, person.getAge());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13603166666", person.getMobile().getNumber());
    }
}
```

### B. Register managed objects manually:

```java
    @Test
    public void testRegisterManually(){
        var context = Context.make(CONFIGS);
        var factory = context.createFactory();
        factory.register(Person.class);
        factory.register(Mobile.class);
        factory.start();
        
        //Chain-style calling:
        //factory.register(Person.class, Mobile.class).start();
        
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals(18, person.getAge());
        TestCase.assertEquals("John Denver", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13603166666", person.getMobile().getNumber());
    }
```
### C. Load classes and dependencies from a given JAR file:
We assume to packed these 2 classes into a JAR file "/tmp/project/demo/lib/demo.jar"
```
    @Test
    public void testLoadFromJAR(){
    	var context = Context.make(CONFIGS);
    	var factory = context.createFactory();
    	factory.load("/tmp/project/demo/lib/demo.jar");
    	factory.start();
       
   		var person = context.get(Person.class);
    	var mobile = context.get(Mobile.class);
    	TestCase.assertEquals(18, person.getAge());
     	TestCase.assertEquals("John Denver", person.getName());
    	TestCase.assertEquals("+86", mobile.getAreaCode());
    	TestCase.assertEquals("13603166666", person.getMobile().getNumber());
    }
```
### D.  Parse the XML Definition (beans.xml)

If you are a Spring Framework developer, you will be very familiar with XML configuration. Whale also supports you defining the manged objects in the XML file located in "**/tmp/project/demo/beans.xml**":
```xml
<beans>
	<bean id="person" singleton="true" type="whale.demo.Person">
    	<props>
			<prop name="id" val="45" />
			<prop name="name" key="user.name" />
    		<prop name="mobile" ref="mobile" />
    	</props>
        <methods>
        	<method name="setAge">
            	<arg key="user.age" type="int" />
            </method>
        </methods>
    </bean>
    <bean id="mobile" singleton="true" type="whale.demo.Mobile">
        <args>
	    	<arg key="mobile.area" type="String" />
	    	<arg key="mobile.number" type="String" />
	    </args>
	</bean>
</beans> 
```
Please note that XML definition just supports field injection(using the props tag), constructor injection(using the args tag) and method injection(using the methods tag). For the constructor and method injection, you must explicitily declare the parameter types. Othewise, whale may not be able to correctly identify overloaded methods in certain situations. For example:

```java
//In constructor:
public Student(String studentNumber);
public Student(int age);
//How do we correctly explain the value "21" from configuration? 

//In general methods:
public void setScore(int age);
public void setScore(float age);
//We can convert the value "85" to 85(int) or 85.0(float). Which method will be invoked? 
```

More advanced features are forbidden because it makes the XML schema very ugly.

```java
    @Test
    public void testParseXMLDefinition(){
    	var context = Context.make(CONFIGS);
     	var factory = context.createFactory();
      	factory.parse("/tmp/project/demo/beans.xml");
      	factory.start();
       	
       	//Chain-stype calling
       	//context.createFactory().parse("/tmp/project/demo/beans.xml").start();
       	
       	var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        
        TestCase.assertEquals(18, person.getAge());
        TestCase.assertEquals("John Denver", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13603166666", person.getMobile().getNumber());
    }
```
You can actually pass multiple XML definitions to the method parse. For example:
```java
    factory.parse("/tmp/project/demo/beans-1.xml", "/tmp/project/demo/beans-2.xml");
```

## 4. Provider<T>

The Provider interface  is similar to the **ObjectFactory** in Spring Framework. One of its primary benefits is resolving the circular dependent. For example:

```java
// In class Person:
@Inject
private Mobile mobie;

//In class Mobile
@Inject
private Person owner;
```
Whale cannot assemble the above 2 objects and throws an exception "Circular dependent is detected". We can refactor it using Provider interface as following:
```java
// In class Person:
@Inject
private Provider<Mobile> mobile;

//In class Mobile
@Inject
private Provider<Person> owner;

//In test case:
var name = mobile.getOwner().get().getName();
var code = person.getMobile().get().getAreaCode();
```
Now, it works correctly. Please note that you should avoid calling the method Provider.get() directly within constructor or other method injection, Doing so can lead to unexpected behavior:

```java
private Mobile mobile;

@Inject
public Person(Provider<Mobile> mobile){
	this.mobile = mobile.get(); //Here
}
```

A correct approach is as below:

```java
private Provider<Mobile> mobile;

@Inject
public Person(Provider<Mobile> mobile){
	this.mobile = mobile;
}
```

More information about Provider please refer to the document on github.

[JSR330]: https://github.com/javax-inject/javax-inject

## 5. Advanced Features

The section describes some advanced features in whale. It helps developers more flexibilities.

### A. Bind Annotation

Please consider the following example code:

```java
package whale.demo.service;

public interface DemoService{
	public Object doSomething(Object args);
}

package whale.demo.service;
@Singleton
public class DemoServiceImpl implements DemoService{
	public Object doSomething(Object args){
		Object result = handle_your_business();
		return result;
	}
}
```

Since the interface DemoService can not be instantiated directly, we must register the implementation class DemoServiceImpl as a managed object into DI container.

```java
//The first approach:
public class Demo{
	@Inject
	@Named("whale.demo.service.DemoServiceImpl")
	private DemoService service;
}

//The second approach:
public class Demo{
	@Inject
	private DemoServiceImpl service;
}
```

The first approach ontlined above is overly verbose, and the second approach deviates from the principles of Interface-Oriented programming. Bind annotation offers a more concise and elegant way for developer, as demonstrated below:

```java
package whale.demo.service;

@Bind(target=DemoService.class)
public interface DemoService{
	public Object doSomething(Object args);
}

public class Demo{
	@Inject
	private DemoService service;
}
```

Certainly, you can call the bind method manually in code:

```java
factory.bind(DemoService.class, DemoServiceImpl.class);
```

To summarize, the Bind annotation provides a straightforward way for mapping an abstraction (interface or abstract class) to its concrete implementation, simplifying the dependence configuration.

### B. Append Managed Object

Whale offers the flexibility to append managed objects into DI container even after the container has been initialized.

```java
 @Test
    public void testAppendBeans(){
        var context = Context.make(CONFIGS);
        var factory = context.createFactory();
        factory.register(Person.class);
        factory.register(Mobile.class);
        factory.start(); //Container Initialized
        
      	factory.append(DemoServiceImpl.class);
        
        var person = context.get(Person.class);
        var mobile = context.get(Mobile.class);
        TestCase.assertEquals(18, person.getAge());
        TestCase.assertEquals("John Denver", person.getName());
        TestCase.assertEquals("+86", mobile.getAreaCode());
        TestCase.assertEquals("13603166666", person.getMobile().getNumber());
    }
```

If the append() method is invoked before the start() method, whale will disregard the call.

### C. Customized Qualifier Annotation

As mentioned earlier, the Qualifier is a meta-annotation, so it cannot be used directly. Developers can create custom annotations that extend from it. Let's illustrate this with an example:

```java
//User defines two qualifier annotations
//The first
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Cat {
}

//The second
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Dog {
}

//Declare an interface
public interface Animal{
	public String howl();
}

//There are 2 implements of above interface: Cat and Dog
@Cat
public class Cat implements Animal{
	public String howl(){
		return "Miao Miao";
	}
}

@Dog
public class Dog implements Animal{
	public String howl(){
		return "Wang Wang";
	}
}

// The class Family dependents on the implementation Cat and Dog
public class Family{
	private Person father;
	private Person mother;
    private Person child;
	
    @Inject
    @Cat
    private Animal cat;
    
    @Inject
    @Dog
    private Animal dog;
}
```

Whale will seamlessly inject the correct implementation into the `cat` and `dog` properties, eliminating the need for verbose configuration. While the Bind annotation associates an implementation with an interface, it's not well-suited for scenarios with multiple implementations. Custom Qualifiers provide a more effective way to specify and inject different implementations, offering a more elegant solution.

## 6. Web Application

The WebListener class enables the intergation of whale into a web application. Please add a listener declaration(using the listener tag) in web.xml file:

```xml
<listener>
	<listener-class>cn.techarts.whale.web.WebListener</listener-class>
</listener>
```
Now, you can retrieve the managed objects from the servlet context, as demonstrated below: 
```java
public DemoServlet extends HttpServlet{
	public void init(ServletConfig arg) {
		var context = Context.from(arg.getServletContext());
		DemoService service = context.get(DemoSrevice.class);	
		service.doSomething(context.get(Person.class));
	}
}
```

## 7. Todo List
We plan to add the following features:
- Interceptor and Enhancer annotations.
- Refactor code to improve performance.
- Fix bugs as soon as they are found.