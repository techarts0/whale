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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.techarts.whale.Panic;
import cn.techarts.whale.util.Hotpot;
import cn.techarts.whale.util.Scanner;

/**
 * The core class of whale. It's responsible to<p>
 * 1. Load, scan and parse managed objects from JAR file, class-path and XML file.<br>  
 * 2. Resolve dependent relation and register them into container. <br>
 * 3. Assemble these managed objects and create instance.
 * 
 * @author rocwon@gmail.com
 */
public class Factory {
	private boolean launched = false;
	private Map<String, Craft> crafts;
	private Map<String, Craft> material;
	private Map<String, String> configs;
	
	private static final Logger LOGGER = Hotpot.getLogger();
	
	public Factory(Map<String, Craft> container, Map<String, String> configs) {
		if(container == null) {
			throw Panic.nullContainer();
		}
		this.crafts = container;
		material = new ConcurrentHashMap<>(256);
		this.configs = configs != null ? configs : Map.of();
	}
	
	/**
	 * <b>IMPORTANT: The method can only be called ONCE!</b>
	 */
	public void start() {
		if(this.launched) {
			throw Panic.factoryInitialized();
		}
		this.assembleAndInstanceManagedCrafts();
		this.launched = true; //The method can only be called ONCE.
		LOGGER.info("The DI container is initialized (" + crafts.size() + " beans)");
	}
	
	private void resolveJSR330BasedCrafts(String... classpath) {
		if(classpath == null || classpath.length == 0) return;
		for(int i = 0; i < classpath.length; i++) {
			this.scanAndResolveCrafts(classpath[i]);
		}
	}
	
	private void resolveConfigBasedCrafts(String... xmlResources) {
		if(xmlResources == null || xmlResources.length == 0) return;
		for(int i = 0; i < xmlResources.length; i++) {
			this.parseAndResolveXMLCrafts(xmlResources[i]);
		}
	}
	
	/**
	 * Load and register managed beans from multiple JAR files.
	 */
	public Factory load(String[] jars) {
		if(jars == null) return this;
		if(jars.length == 0) return this;
		for(var jar : jars) load(jar);
		return this;
	}
	
	/**
	 * Load and register managed beans from a JAR file.
	 */
	public Factory load(String jar) {
		if(jar == null) return this;
		var classes = Scanner.scanJar(jar);
		if(classes == null) return this;
		if(classes.isEmpty()) return this;
		for(var clazz : classes) {
			this.register(clazz);
		}
		return this;
	}
	
	/**
	 * Scan the specified multiple class-paths to register managed objects.
	 */
	public Factory scan(String[] classpaths) {
		resolveJSR330BasedCrafts(classpaths);
		return this;
	}
	
	/**
	 * Scan the specified single class-path to register managed objects.
	 */
	public Factory scan(String classpath) {
		resolveJSR330BasedCrafts(classpath);
		return this;
	}
	
	/**
	 * Parse the specified multiple XML files to register managed objects.
	 */
	public Factory parse(String[] xmlResources) {
		resolveConfigBasedCrafts(xmlResources);
		return this;
	}
	
	/**
	 * Parse the specified single XML file to register managed objects.
	 */
	public Factory parse(String xmlResource) {
		resolveConfigBasedCrafts(xmlResource);
		return this;
	}
	
	/**
	 * Append a managed bean instance into IOC container.
	 */
	public Factory register(Object... beans) {
		if(beans == null) return this;
		if(beans.length == 0) return this;
		for(var bean : beans) {
			this.register(bean);
		}
		return this;
	}
	
	/**
	 * Append a managed bean into IOC container by class.
	 */
	public Factory register(Class<?>... beans) {
		if(beans == null) return this;
		if(beans.length == 0) return this;
		for(var bean : beans) {
			this.register(bean);
		}
		return this;
	}
	
	/**
	 * Append a managed bean into IOC container by class name.
	 */
	public Factory register(String... classes) {
		if(classes == null) return this;
		if(classes.length == 0) return this;
		for(var clazz : classes) {
			this.register(clazz);
		}
		return this;
	}
	
	private void register(String clzz) {
		if(clzz == null) return;
		var result = toCraft(clzz);
		if(result == null) return;
		if(!result.isManaged()) return;
		material.put(result.getName(), result);
	}
	
	private void register(Object bean) {
		if(bean == null) return;
		var craft = toCraft(bean.getClass());
		craft.setInstance(bean);
		material.put(craft.getName(), craft);
	}

	private void register(Class<?> clazz) {
		if(clazz == null) return;
		var craft = toCraft(clazz);
		material.put(craft.getName(), craft);
	}
	
	private void register(Craft craft) {
		material.put(craft.getName(), craft);
	}
	
	private Craft toCraft(Class<?> clazz) {
		if(!Hotpot.newable(clazz)) return null;
		var named = clazz.getAnnotation(Named.class);
		var s = clazz.isAnnotationPresent(Singleton.class);
		var explicitly = named != null || s;
		if(explicitly == false) return null;
		//Bean id: the qualifier name is first
		var name = named != null ? named.value() : ""; 
		if(name.isEmpty()) name = clazz.getName();
		return new Craft(name, clazz, s, explicitly);			
	}
	
	private Craft toCraft(String className) {
		try {
			return toCraft(Class.forName(className));
		}catch(ClassNotFoundException e) {
			throw Panic.classNotFound(className, e);
		}
	}
	
	/**Crafts defined in XML file.*/
	private void parseAndResolveXMLCrafts(String resource){
		if(resource == null|| resource.isBlank()) return;
		try {
			var factory = DocumentBuilderFactory.newInstance();
			var stream = new FileInputStream(resource);
			var doc = factory.newDocumentBuilder().parse(stream);
	        doc.getDocumentElement().normalize();
	        var crafts = doc.getElementsByTagName("bean");
	        if(crafts == null || crafts.getLength() == 0) return;
	        for(int i = 0; i < crafts.getLength(); i++) {
	        	register(xmlBean2Craft(crafts.item(i)));
	        }
		}catch(Exception e) {
			throw Panic.failed2ParseJson(resource, e);
		}
	}
	
	private void scanAndResolveCrafts(String classpath) {
		if(classpath == null || classpath.isBlank()) return;
		var base = new File(classpath);//Root class-path
		if(base == null || !base.isDirectory()) return;
		var start = base.getAbsolutePath().length();
		var classes = Scanner.scanClasses(base, start);
		classes.forEach(clazz->this.register(clazz));
	}
	
	private void assembleAndInstanceManagedCrafts() {
		var start = material.size();
		if(start == 0) return; //Assemble Completed
		for(var entry : material.entrySet()) {
			var craft = entry.getValue();
			craft.inject(crafts, material, configs);
			craft.construct().assemble().execute();
			if(craft.isAssembled()) {
				var key = entry.getKey();
				this.crafts.put(key, craft);
				this.material.remove(key);
			}
		}
		if(start == material.size()){ //Not Empty
			throw Panic.circularDependence(dump());
		}
		this.assembleAndInstanceManagedCrafts();
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
	
	private void parseArgs(NodeList args, Craft result) {
		if(args == null || args.getLength() != 1) return;
		var first = (org.w3c.dom.Element)args.item(0);
		args = first.getElementsByTagName("arg");
		if(args == null || args.getLength() == 0) return;
		for(int i = 0; i < args.getLength(); i++) {
			var arg = args.item(i);
			if(arg.getNodeType() != Node.ELEMENT_NODE) continue;
			var injector = xmlNode2Injector((Element)arg);
			result.addArgument(i, injector);
		}
	}
	
	private void parseProps(NodeList props, Craft result) {
		if(props == null || props.getLength() != 1) return;
		var first = (org.w3c.dom.Element)props.item(0);
		props = first.getElementsByTagName("prop");
		if(props == null || props.getLength() == 0) return;
		var fields = new HashMap<String, Field>();
		getFields(fields, Hotpot.forName(result.getType()));
		for(int i = 0; i < props.getLength(); i++) {
			var prop = props.item(i);
			if(prop.getNodeType() != Node.ELEMENT_NODE) continue;
			var tmp = (Element)prop;
			var name = tmp.getAttribute("name");
			var injector = xmlNode2Injector(tmp);
			result.addProperty(fields.get(name), injector);
		}
	}
	
	private Injectee xmlNode2Injector(Element node) {
		var ref = node.getAttribute("ref");
		var key = node.getAttribute("key");
		var val = node.getAttribute("val");
		var type = node.getAttribute("type");
		return Injectee.of(ref, key, val, type);
	}
	
	private Craft xmlBean2Craft(Node node) {
		if(node.getNodeType() != Node.ELEMENT_NODE) return null;
		var craft = (org.w3c.dom.Element)node;
		var result = new Craft(craft.getAttribute("type"));
		result.setName(craft.getAttribute("id"));
		result.setSingleton(craft.getAttribute("singleton"));
		parseArgs(craft.getElementsByTagName("args"), result);
		parseProps(craft.getElementsByTagName("props"), result);
		//XML DOES NOT SUPPORT METHOD INJECTION
		return result.withConstructor();
	}
	
	private void getFields(Map<String, Field> result, Class<?> clazz) {
		if(clazz == null) return; //Without super class
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				result.put(f.getName(), f);
			}
		}
		getFields(result, clazz.getSuperclass());
	}
}