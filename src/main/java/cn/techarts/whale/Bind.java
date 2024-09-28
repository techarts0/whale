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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 *Bind a qualifier name to an exist managed object. 
 *It's often used to bind an interface or abstract class to an implementation.
 * 
 * @author rocwon@gmail.com
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Bind {
	
	/**
	 * An interface or abstract class.
	 */
	public String value() default "";
	
	/**
	 * An implementation class.
	 */
	public Class<?> target();
}
