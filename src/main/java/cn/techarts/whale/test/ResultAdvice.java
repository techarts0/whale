package cn.techarts.whale.test;

import cn.techarts.whale.Advice;

public class ResultAdvice implements Advice{

	@Override
	public Object execute(Object[] args, Object result, Throwable threw) {
		var tmp = (Integer)result;
		return tmp + 100;
	}
	
}
