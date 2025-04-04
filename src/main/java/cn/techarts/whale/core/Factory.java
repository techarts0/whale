/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.whale.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import cn.techarts.whale.Bind;
import cn.techarts.whale.Panic;
import cn.techarts.whale.util.Hotpot;
import cn.techarts.whale.util.Scanner;

/**
 * The core class of whale. It's responsible for<p>
 * 1. Load, scan and parse managed objects from JAR file, class-path and XML file.<br>  
 * 2. Resolve dependent relations and register them into container. <br>
 * 3. Assemble these managed objects and create instance.
 * 
 * @author rocwon@gmail.com
 */
public class Factory implements Binder, Loader{
	private boolean launched = false;
	private Map<String, Craft> crafts;
	private Map<String, Craft> material;
	private Map<String, String> configs;
	private Map<String, String> binders; //Target->Source
	
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public Factory(Map<String, Craft> container) {
		this(container, null);
	}
	
	public Factory(Map<String, Craft> container, Map<String, String> configs) {
		if(Objects.isNull(container)) {
			throw Panic.nullContainer();
		}
		this.crafts = container;
		this.binders = new HashMap<>(32);
		this.material = new ConcurrentHashMap<>(256);
		this.configs = configs != null ? configs : Map.of();
	}
	
	/**
	 * <b>IMPORTANT: The method can only be called ONCE!</b>
	 */
	public void launch() {
		if(this.launched) return;
		this.assembleAndInstanceCrafts();
		this.launched = true; //The method can only be called ONCE.
		LOGGER.info("Whale is initialized successfully (" + crafts.size() + " managed objects).");
	}
	
	private void resolveJSR330BasedCrafts(String... classpath) {
		if(Objects.isNull(classpath)) return;
		for(int i = 0; i < classpath.length; i++) {
			this.scanAndResolveCrafts(classpath[i]);
		}
	}
	
	private void resolveXmlConfigBasedCrafts(String... xmlResources) {
		if(Hotpot.isNull(xmlResources)) return;
		for(int i = 0; i < xmlResources.length; i++) {
			this.loadAndResolveXMLCrafts(xmlResources[i]);
		}
	}
	
	/**
	 * Load and register managed beans from multiple JAR files.
	 */
	@Override
	public Loader load(String[] jars) {
		if(launched) return this;
		if(Objects.isNull(jars)) return this;
		if(jars.length == 0) return this;
		for(var jar : jars) load(jar);
		return this;
	}
	
	/**
	 * Load and register managed beans from a JAR file.
	 */
	@Override
	public Loader load(String jar) {
		if(launched) return this;
		if(Hotpot.isNull(jar)) return this;
		var classes = Scanner.scanJar(jar);
		if(Objects.isNull(classes)) return this;
		if(classes.isEmpty()) return this;
		for(var clazz : classes) {
			this.register(clazz);
		}
		return this;
	}
	
	/**
	 * Scan the specified multiple class-paths to register managed objects.
	 */
	@Override
	public Loader scan(String[] classpaths) {
		if(this.launched) return this;
		resolveJSR330BasedCrafts(classpaths);
		return this;
	}
	
	/**
	 * Scan the specified single class-path to register managed objects.
	 */
	@Override
	public Loader scan(String classpath) {
		if(this.launched) return this;
		resolveJSR330BasedCrafts(classpath);
		return this;
	}
	
	/**
	 * Parse the specified multiple XML files to register managed objects.
	 */
	@Override
	public Loader parse(String[] xmlResources) {
		if(this.launched) return this;
		resolveXmlConfigBasedCrafts(xmlResources);
		return this;
	}
	
	/**
	 * Parse the specified single XML file to register managed objects.
	 */
	@Override
	public Loader parse(String xmlResource) {
		if(this.launched) return this;
		resolveXmlConfigBasedCrafts(xmlResource);
		return this;
	}
	
	/**
	 * Append a managed bean instance into DI container.
	 */
	@Override
	public Binder register(Object... beans) {
		if(this.launched) return this;
		if(Objects.isNull(beans)) return this;
		if(beans.length == 0) return this;
		for(var bean : beans) {
			this.register(bean);
		}
		return this;
	}
	
	@Override
	public Binder register(List<String> classes) {
		if(this.launched) return this;
		if(Objects.isNull(classes)) return this;
		if(classes.isEmpty()) return this;
		for(var clazz : classes) {
			this.register(clazz);
		}
		return this;
	}
	/**
	 * Append a managed bean into DI container by class.
	 */
	@Override
	public Binder register(Class<?>... beans) {
		if(this.launched) return this;
		if(Objects.isNull(beans)) return this;
		if(beans.length == 0) return this;
		return this.register0(beans);
	}
	
	/**
	 * Append a managed bean into IOC container by class name.
	 */
	@Override
	public Binder register(String... classes) {
		if(this.launched) return this;
		if(Objects.isNull(classes)) return this;
		if(classes.length == 0) return this;
		for(var clazz : classes) {
			this.register(clazz);
		}
		return this;
	}
	
	/**
	 * Bind an interface/abstract class to an implementation class and register it into the factory.
	 */
	@Override
	public Binder bind(String abstraction, String implementation) {
		if(this.launched) return this;
		if(Hotpot.orNull(abstraction, implementation)) return this;
		this.binders.put(implementation, abstraction);
		this.appendMaterial(this.toCraft(implementation));
		return this;
	}
	
	/**
	 * Bind an interface/abstract class to an implementation class and register it into the factory.
	 */
	@Override
	public Binder bind(String abstraction, Class<?> implementation) {
		if(this.launched) return this;
		if(Hotpot.orNull(abstraction, implementation)) return this;
		this.binders.put(implementation.getName(), abstraction);
		this.appendMaterial(this.toCraft(implementation));
		return this;
	}
	
	/**
	 * Bind an interface/abstract class to an implementation class and register it into the factory.
	 */
	@Override
	public Binder bind(Class<?> abstraction, Class<?> implementation) {
		if(this.launched) return this;
		if(Hotpot.orNull(abstraction, implementation)) return this;
		this.binders.put(implementation.getName(), abstraction.getName());
		this.appendMaterial(this.toCraft(implementation));
		return this;
	}
	
	@Override
	public Binder include(Object obj, String...name) {
		if(this.launched) return this;
		if(Objects.isNull(obj)) return this;
		String key = null;
		if(name != null && name.length == 1) {
			key = name[0];
		}else {
			key = obj.getClass().getName();
		}
		appendMaterial(new Craft(key, obj));
		return this;
	}
	
	/**
	 * Append managed objects into context after calling {@link start()}.<br>
	 * The method equals the statement {@code register(classes).start();} <br>
	 * Dont't call it directly on a factory instance, call the {@link Context.append()} instead. 
	 */
	@Override
	public void append(Class<?>... classes) {
		if(!this.launched) return;
		if(!material.isEmpty()) return;
		this.register0(classes);
		this.assembleAndInstanceCrafts();
	}
	
	/**
	 * Append a managed bean into IOC container by class.
	 */
	private Factory register0(Class<?>... beans) {
		for(var bean : beans) {
			this.register(bean);
		}
		return this;
	}
	
	private void appendMaterial(Craft craft) {
		if(Objects.isNull(craft)) return;
		var name = craft.getName();
		material.put(name, craft);
		var bind = binders.get(name);
		if(bind != null) {
			material.put(bind, craft);
		}		
	}
	
	private void register(String clzz) {
		if(Objects.isNull(clzz)) return;
		var result = toCraft(clzz);
		if(Objects.isNull(result)) return;
		this.appendMaterial(result);
	}
	
	private void register(Object bean) {
		if(Objects.isNull(bean)) return;
		var result = toCraft(bean.getClass());
		if(Objects.isNull(result)) return;
		result.setInstance(bean);
		this.appendMaterial(result);
	}

	private void register(Class<?> clazz) {
		if(Objects.isNull(clazz)) return;
		var result = toCraft(clazz);
		this.appendMaterial(result);
	}
	
	private void register(Craft craft) {
		this.appendMaterial(craft);
	}
	
	private void ifBindAnnotaionPresents(Class<?> clazz) {
		var bind = clazz.getAnnotation(Bind.class);
		if(Objects.isNull(bind)) return;
		var src = clazz.getName();
		if(bind.value() != Bind.class) {
			src = bind.value().getName();
		}
		binders.put(bind.target().getName(), src);
	}
	
	private Craft toCraft(Class<?> clazz) {
		ifBindAnnotaionPresents(clazz);
		if(!Hotpot.newable(clazz)) return null;
		var analyzer = new Analyzer(clazz.getDeclaredAnnotations(), 2, clazz.getName());
		return analyzer.isManagedObject() ? new Craft(clazz, analyzer) : null;
	}
	
	private Craft toCraft(String className) {
		try {
			return toCraft(Class.forName(className));
		}catch(ClassNotFoundException e) {
			throw Panic.classNotFound(className, e);
		}
	}
	
	/**Crafts defined in XML file.*/
	private void loadAndResolveXMLCrafts(String resource){
		if(Hotpot.isNull(resource)) return;
		try {
			var factory = DocumentBuilderFactory.newInstance();
			var stream = new FileInputStream(resource);
			var doc = factory.newDocumentBuilder().parse(stream);
	        doc.getDocumentElement().normalize();
	        var crafts = doc.getElementsByTagName("bean");
	        if(Objects.isNull(crafts)) return;
	        for(int i = 0; i < crafts.getLength(); i++) {
	        	register(new XmlBean().toCraft(crafts.item(i)));
	        }
		}catch(Exception e) {
			throw Panic.failed2ParseJson(resource, e);
		}
	}
	
	private void scanAndResolveCrafts(String classpath) {
		if(Hotpot.isNull(classpath)) return;
		var base = new File(classpath);//Root class-path
		if(!base.isDirectory()) return;
		var start = base.getAbsolutePath().length();
		var classes = Scanner.scanClasses(base, start);
		classes.forEach(clazz->this.register(clazz));
	}
	
	private Factory assembleAndInstanceCrafts() {
		var quantity = material.size();
		if(quantity == 0) return this; //Assemble Completed
		for(var entry : material.entrySet()) {
			var craft = entry.getValue();
			if(!craft.isAssembled()) {
				craft.inject(crafts, material, configs);
				craft.construct().assemble().execute();
			}
			if(craft.isAssembled()) {
				var key = entry.getKey();
				this.crafts.put(key, craft);
				this.material.remove(key);
			}
		}
		if(quantity == material.size()){ //Not Empty
			throw Panic.circularDependence(dump());
		}
		return this.assembleAndInstanceCrafts();
	}
	
	private String dump() {
		var result = new StringBuilder();
		material.keySet().forEach(key->{
			result.append(key).append(", ");
		});
		var start = result.length() - 2;
		result.delete(start, start + 2);
		return result.toString();
	}
	
	public void reset() {
		this.launched = false;
		this.binders.clear();
	}
	
	public boolean launched() {
		return this.launched;
	}
}