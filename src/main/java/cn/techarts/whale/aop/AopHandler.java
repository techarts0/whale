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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.techarts.whale.Panic;

/**
 * @author rocwon@gmail.com
 */
public class AopHandler implements InvocationHandler {
	private Object target;
	
	public AopHandler(Object target) {
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		var advise = method.getAnnotation(Advise.class);
		if(advise == null) {
			return method.invoke(target, args);
		}
		var last = getAdvisor(advise.last());
		var threw = getAdvisor(advise.threw());
		var after = getAdvisor(advise.after());
		var before = getAdvisor(advise.before());
		
		try{
			if(before != null) {
				before.advise(args, null, null);
			}
			var result = method.invoke(target, args);
			if(after == null) return result;
			return after.advise(args, result, null);
		}catch(Throwable e) {
			if(threw != null) {
				return threw.advise(args, null, getEx(e));
			}else {
				throw new Panic("Failed to intercept the method.", e);
			}
		}finally {
			if(last != null) {
				return last.advise(args, null, null);
			}
		}
	}
	
	private Advisor getAdvisor(Class<? extends Advisor> arg) {
		if("ZeroAdvisor".equals(arg.getSimpleName())) {
			return null;
		}
		try {
			return arg.getDeclaredConstructor().newInstance();
		}catch(Exception e) {
			throw new RuntimeException("Failed to instance the advice.", e);
		}
	}
	
	private Throwable getEx(Throwable e) throws Throwable {
		if(e instanceof InvocationTargetException) {
			return e.getCause(); //Business Exception
		}else if(e instanceof IllegalAccessException) {
			throw e; //It is not about your business.
		}else if(e instanceof IllegalArgumentException) {
			throw e; //It is not about your business.
		}else {
			return e; //The exception threw by target method
		}
	}
	
	public static Object create(Object target, Class<?> t) {
		if(t == null) return target;
		if(!t.isInterface()) {
			throw Panic.notAnInterface(t);
		}
		var cl = target.getClass().getClassLoader();
		var ifs = target.getClass().getInterfaces();
		if(ifs == null || ifs.length == 0)return target;
		var handler = new AopHandler(target);
		return t.cast(Proxy.newProxyInstance(cl, ifs, handler));
	}
}