package cn.techarts.whale.test;

import java.util.Map;
import org.junit.Test;
import cn.techarts.whale.Context;
import junit.framework.TestCase;

public class WhaleTest {
	private static final String CLASSPATH = "/D:/Project/java/whale/target/classes"; //Your class path
	private static final Map<String, String> CFG = Map.of("zone", "+86", "user.id", "45", 
			"build.name", "Library", "user.name", "Johnson", "party.name", "Republic");
	
	//@Test
	public void testRegisterManually() {
		var ctx = Context.make(CFG);
		var binder = ctx.getBinder();
		binder
		.register(Person.class, Mobile.class)
		.register(People.class)
		.bind(SomeInterface.class, SomeInterfaceImpl.class)
		.register(Office.class);
		
		ctx.start();
		
		binder.append(Party.class);
		
		var p = ctx.get(Person.class);
		var m = ctx.get(Mobile.class);
		var o = ctx.get(Office.class);
		var t = ctx.get(Party.class);
		var s = ctx.get(SomeInterface.class);
		
		ctx.close();
		
		TestCase.assertNotNull(s);
		TestCase.assertNotNull(t);
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
		
		
	}
	
	//@Test
	public void testScanClasspath() {
		var ctx = Context.make(CFG);
		var loader = ctx.getLoader();
		loader.scan(CLASSPATH);
		
		ctx.start();
		
		var p = ctx.get(Person.class);
		var m = ctx.get(Mobile.class);
		var o = ctx.get(Office.class);
		
		TestCase.assertNotNull(ctx.get(SomeInterface.class));
		TestCase.assertNotNull(ctx.get(Party.class));
		TestCase.assertEquals("+86", m.getZone());
		TestCase.assertEquals(33, p.getService().getValue());
		TestCase.assertEquals(45, m.getContact().getId());
		TestCase.assertEquals(22, p.getOffice().getId());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals("13980092699", o.getMobile().getNumber());
		TestCase.assertEquals("Library", p.getOffice().getBuilding());
		TestCase.assertEquals("+86", p.getOffice().getMobile().getZone());
		TestCase.assertEquals(3, o.getAdmin().getId());
		ctx.close();
	}
	
	//@Test
	public void testParseXML() {
		var ctx = Context.make(CFG);
		var factory = ctx.getLoader();
		factory.parse("D:\\Project\\java\\whale\\src\\main\\java\\cn\\techarts\\whale\\test\\beans.xml");
		ctx.start();
		
		
		//var p = ctx.get(Person.class);
		var t = ctx.get("party", Party.class);
		
		ctx.close();
		TestCase.assertEquals(333, t.getId());
		TestCase.assertEquals(1000, t.getMembers());
		TestCase.assertEquals("Republic", t.getName());
		TestCase.assertEquals("Trump", t.getChairman().getName());
	
	}
	
	//@Test
	public void testIncludeExternalObject() {
		var ctx = Context.make();
		ctx.getBinder().include(new Object());
		ctx.getBinder().include(new Object(), "mydear");
		ctx.start();
		
		var mydear = ctx.get("mydear");
		var plain = ctx.get(Object.class);
		
		TestCase.assertEquals(true, mydear != null);
		TestCase.assertEquals(true, plain != null);
	}
	
	//@Test
	public void testMethodIntercept() {
		var ctx = Context.make();
		ctx.getBinder().register(SomeInterfaceImpl.class);
		ctx.start();
		
		var service = ctx.get(SomeInterface.class);
		var result = service.getValue();
		
		TestCase.assertEquals(133, result);
	}
}