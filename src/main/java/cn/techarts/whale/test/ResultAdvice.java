package cn.techarts.whale.test;

import cn.techarts.whale.aop.Advisor;

public class ResultAdvice implements Advisor{

	@Override
	public Object advise(Object[] args, Object result, Throwable threw) {
		var tmp = (Integer)result;
		return tmp + 100;
	}
	
}
