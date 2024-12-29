package cn.techarts.whale.test;

import javax.inject.Inject;
import javax.inject.Singleton;
import cn.techarts.whale.Bind;

//import jakarta.inject.Inject;
//import jakarta.inject.Named;

import cn.techarts.whale.Valued;
import cn.techarts.whale.aop.Advice;

@Bind(value=SomeInterface.class, target=SomeInterfaceImpl.class)
@Singleton
@Advice(SomeInterface.class)
public class SomeInterfaceImpl implements SomeInterface {
	
	private int val;
	
	@Inject
	public SomeInterfaceImpl(@Valued(val="33")int value) {
		this.val = value;
	}
	
	@Override
	public int getValue() {
		return this.val;
	}
}