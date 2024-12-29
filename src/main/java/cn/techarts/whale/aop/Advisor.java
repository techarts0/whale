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

package cn.techarts.whale.aop;

/**
 * The implementation of interface Advisor MUST have a default constructor.
 * Namely, it MUST be instanced via the operator new without parameters.
 * 
 * @author rocwon@gmail.com
 */
public interface Advisor {
	public Object execute(Object[] args, Object result, Throwable threw);
}

class IgnoredAdvice implements Advisor{
	@Override
	public Object execute(Object[] args, Object result, Throwable threw) {
		return null;
	}
	
}