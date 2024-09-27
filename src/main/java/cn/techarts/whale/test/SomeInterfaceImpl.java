package cn.techarts.whale.test;

import javax.inject.Named;

import cn.techarts.whale.Valued;

@Named
public class SomeInterfaceImpl implements SomeInterface {
	
	@Valued(val="33")
	private int value;
	
	public SomeInterfaceImpl() {
		
	}
}
