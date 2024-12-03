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

package cn.techarts.whale.core;

import java.util.List;

/**
 * @author rocwon@gmail.com
 */
public interface Binder {
	/**
	 * Append managed objects into context after calling {@link start()}.<br>
	 * The method equals the statement {@code register(classes).start();} <br>
	 * Dont't call it directly on a factory instance, call the {@link Context.append()} instead. 
	 */
	public void append(Class<?>... classes);
	
	/**
	 * Bind an interface/abstract class to an implementation class and register it into the factory.
	 */
	public Binder bind(Class<?> abstraction, Class<?> implementation);
	
	/**
	 * Bind an interface/abstract class to an implementation class and register it into the factory.
	 */
	public Binder bind(String abstraction, Class<?> implementation);
	
	/**
	 * Bind an interface/abstract class to an implementation class and register it into the factory.
	 */
	public Binder bind(String abstraction, String implementation);
	
	/**
	 * Append a managed bean into DI container by class.
	 */
	public Binder register(Class<?>... beans);
	
	public Binder register(List<String> classes);
	
	/**
	 * Append a managed bean instance into DI container.
	 */
	public Binder register(Object... beans);
	
	/**
	 * Append a managed bean into IOC container by class name.
	 */
	public Binder register(String... classes);
	
	/**Include an external singleton object(NON-JSR330) into DI container with the specified name*/
	public Binder include(Object obj, String...name);
}
