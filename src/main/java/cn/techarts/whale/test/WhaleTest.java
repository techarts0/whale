package cn.techarts.whale.test;

import java.util.Map;
import org.junit.Test;

import cn.techarts.whale.Context;
import junit.framework.TestCase;

public class WhaleTest {
	private static final String CLASSPATH = "/D:/Studio/Project/Java/whale/target/classes"; //Your class path
	private static final Map<String, String> CFG = Map.of("zone", "+86", "user.id", "45", "build.name", "Library", "user.name", "Johnson");
	
	@Test
	public void testRegisterManually() {
		var ctx = Context.make(CFG);
		var factory = ctx.createFactory();
		factory
		.register(Person.class, Mobile.class)
		.register(People.class)
		.bind(SomeInterface.class, SomeInterfaceImpl.class)
		.register(Office.class)
		.start();
		ctx.append(Party.class);
		
		var p = ctx.get(Person.class);
		var m = ctx.get(Mobile.class);
		var o = ctx.get(Office.class);
		var t = ctx.get(Party.class);
		
		TestCase.assertNotNull(ctx.get(SomeInterface.class));
		TestCase.assertEquals("+86", m.getZone());
		TestCase.assertEquals(33, p.getService().getValue());
		TestCase.assertEquals(45, m.getContact().getId());
		TestCase.assertEquals(22, p.getOffice().getId());
		TestCase.assertEquals(3, o.getStudent().getId());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("13980092699", o.getMobile().getNumber());
		TestCase.assertEquals("Library", p.getOffice().getBuilding());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals(3, o.getAdmin().getId());
		TestCase.assertEquals("Johnson", t.getChairman().getName());
	}
	
	//@Test
	public void testScanClasspath() {
		var ctx = Context.make(CFG);
		var factory = ctx.createFactory();
		factory.scan(CLASSPATH).start();
		var p = ctx.get(Person.class);
		var m = ctx.get(Mobile.class);
		var o = ctx.get(Office.class);
		var t = ctx.get(Party.class);
		
		TestCase.assertNotNull(ctx.get(SomeInterface.class));
		TestCase.assertEquals("+86", m.getZone());
		TestCase.assertEquals(33, p.getService().getValue());
		TestCase.assertEquals(45, m.getContact().getId());
		TestCase.assertEquals(22, p.getOffice().getId());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("13980092699", o.getMobile().getNumber());
		TestCase.assertEquals("Library", p.getOffice().getBuilding());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("+86", o.getAdmin().getId());
		TestCase.assertEquals("Johnson", t.getChairman().getName());
	}
}