package cn.techarts.whale.test;

import cn.techarts.whale.aop.Advisor;

public class LogAdvice implements Advisor {
	@Override
	public Object advise(Object[] args, Object result, Throwable threw) {
		System.out.println("Before: Intercepted");
		return null;
	}
}
