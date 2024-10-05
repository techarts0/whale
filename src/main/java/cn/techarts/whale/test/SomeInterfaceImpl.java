package cn.techarts.whale.test;

import javax.inject.Inject;
import javax.inject.Named;

import cn.techarts.whale.Bind;

//import jakarta.inject.Inject;
//import jakarta.inject.Named;

import cn.techarts.whale.Valued;

@Bind(value=SomeInterface.class, target=SomeInterfaceImpl.class)
@Named
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
