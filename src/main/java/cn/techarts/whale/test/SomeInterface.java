package cn.techarts.whale.test;

import cn.techarts.whale.Bind;
import cn.techarts.whale.aop.Advise;

@Bind(target=SomeInterfaceImpl.class)
public interface SomeInterface {
	
	@Advise(before=LogAdvice.class, after=ResultAdvice.class)
	public int getValue();
}
