package cn.techarts.whale.test;

import cn.techarts.whale.Advice;

public class LogAdvice implements Advice {

	@Override
	public Object execute(Object[] args, Object result, Throwable threw) {
		System.out.println("Before: Intercepted");
		return null;
	}

}
