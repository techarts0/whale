package cn.techarts.whale.test;

import cn.techarts.whale.Advise;
import cn.techarts.whale.Bind;

@Bind(target=SomeInterfaceImpl.class)
public interface SomeInterface {
	
	@Advise(before=LogAdvice.class, after=ResultAdvice.class)
	public int getValue();
}
