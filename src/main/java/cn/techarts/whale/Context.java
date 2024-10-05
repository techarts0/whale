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

package cn.techarts.whale;

import java.util.Map;
import java.util.logging.Logger;
import java.util.HashMap;

import javax.servlet.ServletContext;

import cn.techarts.whale.core.Craft;
import cn.techarts.whale.core.Factory;
import cn.techarts.whale.util.Hotpot;

/**
 * It represents the IOC container. <p>
 *
 * @author rocwon@gmail.com
 */
public class Context implements AutoCloseable{
	private Map<String, Craft> crafts;
	private Map<String, String> configs;
	private boolean factoryCreated = false;
	public static final String NAME = "context.whale.techarts";
	private static final Logger LOGGER = Hotpot.getLogger();
	
	private Context(Map<String, Craft> container, Map<String, String> configs){
		this.configs = configs == null ? Map.of() : configs;
		this.crafts = container == null ? Map.of() : container;
	}
	
	/**
	 * Construct an empty context.
	 */
	public static Context make() {
		var container = new HashMap<String, Craft>(256);
		return new Context(container, Map.of());
	}
	
	/**
	 * Construct a context with configuration path.
	 */
	public static Context make(String configPath) {
		var container = new HashMap<String, Craft>(256);
		var configs = Hotpot.resolveProperties(configPath);
		return new Context(container, configs);
	}
	
	/**
	 * Construct a context with configuration.
	 */
	public static Context make(Map<String, String> configs) {
		return new Context(new HashMap<>(256), configs);
	}
	
	/**
	 * Create a bean factory to register/bind beans manually.<p>
	 * If the factory has been created, null will be returned.
	 */
	public Factory createFactory() {
		if(factoryCreated) return null;
		this.factoryCreated = true; //ONCE
		return new Factory(crafts, configs);
	}
	
	/**
	 * Clean up all managed objects and restore the initial state.
	 * you should call {@link createFactory} to rebuild these beans.
	 */
	public Context reset() {
		if(factoryCreated) {
			this.cleanup();
			factoryCreated = false;
		}
		return this;
	}
	
	/**
	 * Retrieve the context from SERVLET context.(Web Application)
	 */
	public static Context from(ServletContext context) {
		if(context == null) return null;
		var obj = context.getAttribute(NAME);
		if(obj == null) return null;
		if(!(obj instanceof Context)) return null;
		return (Context)obj;
	}
	
	/**
	 * Append managed objects after context initialized.
	 */
	public void append(Class<?>... classes) {
		if(factoryCreated == false) return;
		if(classes == null || classes.length == 0) return;
		new Factory(this.crafts, this.configs).append(classes);
	}
	
	/**
	 * Get the managed object from the context.
	 */
	public <T> T get(String name, Class<T> t) {
		var result = get(name);
		return result != null ? t.cast(result) : null;
	}
	
	/**
	 * Get the managed object from the context.
	 */
	public Object get(String name) {
		if(name == null) {
			throw Panic.nullName();
		}
		var craft = crafts.get(name);
		if(craft == null) {
			throw Panic.classNotFound(name);
		}
		return craft.getInstance();
	}
	
	/**
	 * Get the managed object from context without exception.
	 */
	public Object silent(String name) {
		if(name == null) return null;
		var craft = crafts.get(name);
		if(craft == null) return null;
		return craft.getInstance();
	}
	
	/**
	 * Get the managed object without qualifier name.
	 */
	public<T> T get(Class<T> clazz) {
		return get(clazz.getName(), clazz);
	}
	
	public Map<String, Object> all(){
		if(this.crafts == null) return Map.of();
		if(this.crafts.isEmpty()) return Map.of();
		var result = new HashMap<String, Object>();
		for(var entry : crafts.entrySet()) {
			var val = entry.getValue();
			result.put(entry.getKey(), val.getInstance());
		}
		return result;
	}
	
	/**
	 * Export the configuration the container held.
	 */
	public String getConfig(String key) {
		if(key == null) return null;
		if(configs == null) return null;
		return this.configs.get(key);
	}
	
	/**
	 * Cache the IOC context into  SERVLET context.
	 */
	public Context cache(ServletContext context) {
		if(context == null) return this;
		context.setAttribute(NAME, this);
		return this;
	}
	
	@Override
	public void close() {
		this.cleanup();
		this.crafts = null;
		this.configs = null;
		LOGGER.info("The whale context has been cleaned up.");
	}
	
	private void cleanup() {
		if(crafts == null) return;
		if(crafts.isEmpty()) return;
		for(var craft : crafts.values()) {
			craft.destroy();
		}
		this.crafts.clear();
	}
	
}