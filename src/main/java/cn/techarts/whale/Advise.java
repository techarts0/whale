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
 * @author rocwon@gmail.com
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Advise {
	/**
	 * The first statement in the method.
	 */
	public Class<? extends Advice> before() default IgnoredAdvice.class;
	
	/**
	 * The last statement after return statement.
	 */
	public Class<? extends Advice> after() default IgnoredAdvice.class;
	
	/**
	 * Invoke the method when an exception is threw.
	 */
	public Class<? extends Advice> threw() default IgnoredAdvice.class;
}