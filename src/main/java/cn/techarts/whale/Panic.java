/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.whale;

/**
 * Exception of DI
 * 
 * @author rocwon@gmail.com
 */
public class Panic extends RuntimeException {
	
	private static final long serialVersionUID = -6501295436814757979L;
	
	public Panic(String cause) {
		super(cause);
	}
	
	public Panic(String cause, Throwable throwable) {
		super(cause, throwable);
	}
	
	public static Panic nullName() {
		return new Panic("The object name is null");
	}
	
	public static Panic nullContainer() {
		return new Panic("The object container is null!");
	}
	
	public static Panic cannotSetFieldValue(Exception e) {
		return new Panic("Failed to set field value.", e);
	}
	
	public static Panic classNotFound(String name, Throwable e) {
		return new Panic("Can't find the object with name [" + name + "]", e);
	}
	
	public static Panic classNotFound(String name) {
		return new Panic("Can't find the managed object with name [" + name + "]");
	}
	
	public static Panic cannotInstance(String name, Throwable e) {
		return new Panic("Failed to call the constructor of [" + name + "]", e);
	}
	
	public static Panic cannotInvoke(String name, Throwable e) {
		return new Panic("Failed to invoke the method [" + name + "]", e);
	}
	
	public static Panic noQualifier(String name) {
		return new Panic("You must qualify the constructor parameter[" + name + "]");
	}
	
	public static Panic annotationMissing() {
		return new Panic("At least one of Named or Valued annotation is required.");
	}
	
	public static Panic noSingleton() {
		return new Panic("The object is not a singleton.");
	}
	
	public static Panic noDefaultConstructor(Class<?> arg, Throwable e) {
		return new Panic("Need a default constructor of class [" + arg.getName() + "]", e);
	}
	
	public static Panic noDefaultConstructor(Class<?> arg) {
		return new Panic("Need a default constructor of class [" + arg.getName() + "]");
	}
	
	public static Panic circularDependence(String name) {
		return new Panic("Failed to assemble these beans because of circular dependent or dependence missing:\n" + name);
	}
	
	public static Panic configKeyMissing(String key) {
		return new Panic("Can not find the key [" + key + "] in configuration.");
	}
	
	public static Panic typeConvertError(String type, String v, Exception e) {
		return new Panic("Can not convert [" + v + "] to type: " + type, e);
	}
	
	public static Panic unsupportedType(String name) {
		return new Panic("The data type [" + name + "] is unsupported.");
	}
	
	public static Panic failed2ParseJson(String file, Throwable e) {
		return new Panic("Failed to parse the json config: " + file, e);
	}
	
	public static Panic failed2Close(String craft, Throwable e) {
		return new Panic("Failed to close the managed object: " + craft, e);
	}
	
	public static Panic failed2Init(String craft, Throwable e) {
		return new Panic("Failed to initialize the managed object: " + craft, e);
	}
	
	public static Panic failed2ParseXml(String file, Throwable e) {
		return new Panic("Failed to parse the xml config: " + file, e);
	}
	
	public static Panic typeMissing(String arg) {
		return new Panic("The constructor parameter type is required: " + arg);
	}
	
	public static Panic invalidBind(String from, String to) {
		return new Panic("Failed to bind [" + from + "], because [" + to + "] does not exist.");
	}
	
	public static Panic notAnInterface(Class<?> t) {
		return new Panic("The class is not an interface: " + t.getName());
	}
}
