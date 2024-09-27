package cn.techarts.whale.test;

import java.util.Map;
import org.junit.Test;

import cn.techarts.whale.Context;
import junit.framework.TestCase;

public class WhaleTest {
	
	@Test
	public void testProvider() {
		var ctx = Context.make(Map.of("zone", "+86", "user.id", "45", "build.name", "Library"));
		var factory = ctx.createFactory();
		factory.register(Person.class, Mobile.class).register(Office.class).start();
		var p = ctx.get(Person.class);
		var m = ctx.get(Mobile.class);
		var o = ctx.get(Office.class);
		
		TestCase.assertEquals("+86", m.getZone());
		TestCase.assertEquals(45, m.getContact().getId());
		TestCase.assertEquals(22, p.getOffice().getId());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("13980092699", o.getMobile().getNumber());
		TestCase.assertEquals("Library", p.getOffice().getBuilding());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("+86", o.getAdmin().getMobile().getZone());
	}
}