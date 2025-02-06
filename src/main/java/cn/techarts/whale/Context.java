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
import java.util.Objects;
import java.util.logging.Logger;
import java.util.HashMap;

import javax.servlet.ServletContext;

import cn.techarts.whale.core.Binder;
import cn.techarts.whale.core.Craft;
import cn.techarts.whale.core.Factory;
import cn.techarts.whale.core.Loader;
import cn.techarts.whale.util.Hotpot;

/**
 * the DI container. <p>
 * TODO Support jakarta servlet API
 * @author rocwon@gmail.com
 */
public class Context implements AutoCloseable{
	private Map<String, Craft> crafts;
	private Map<String, String> configs;
	private Factory craftFactory = null;
	private static final Logger LOGGER = Hotpot.getLogger();
	public static final String NAME = "context.whale.techarts";
	
	private Context(Map<String, Craft> container, Map<String, String> configs){
		this.crafts = container;
		this.configs = Objects.isNull(configs) ? Map.of() : configs;
		this.craftFactory = new Factory(this.crafts, this.configs);
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
	
	public Binder getBinder() {
		if(craftFactory.launched()) {
			return null;
		}
		return (Binder)this.craftFactory;
	}
	
	public Loader getLoader() {
		if(craftFactory.launched()) {
			return null;
		}
		return (Loader)this.craftFactory;
	}
	
	/**
	 * We strongly recommand you using {@link getBinder} and {@link getLoader} instead.
	 */
	@Deprecated
	public Factory createFactory() {
		if(craftFactory.launched()) {
			return null;
		}
		return this.craftFactory;
	}
	
	/**
	 * Retrieve the context from SERVLET context.(Web Application)
	 */
	public static Context from(ServletContext context) {
		if(Objects.isNull(context)) return null;
		var obj = context.getAttribute(NAME);
		if(Objects.isNull(obj)) return null;
		if(!(obj instanceof Context)) return null;
		return (Context)obj;
	}
	
	/**
	 * For jakarta servlet api.
	 */
	public static Context from(jakarta.servlet.ServletContext context) {
		if(Objects.isNull(context)) return null;
		var obj = context.getAttribute(NAME);
		if(Objects.isNull(obj)) return null;
		if(!(obj instanceof Context)) return null;
		return (Context)obj;
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
		if(Objects.isNull(name)) {
			throw Panic.nullName();
		}
		var craft = crafts.get(name);
		if(Objects.isNull(craft)) {
			throw Panic.classNotFound(name);
		}
		return craft.getInstance();
	}
	
	/**
	 * Get the managed object from context without exception.
	 */
	public Object silent(String name) {
		if(Objects.isNull(name)) return null;
		var craft = crafts.get(name);
		if(Objects.isNull(craft)) return null;
		return craft.getInstance();
	}
	
	/**
	 * Get the managed object from context without exception.
	 */
	public<T> T silent(String name, Class<T> clazz) {
		if(Objects.isNull(name)) return null;
		var craft = crafts.get(name);
		if(Objects.isNull(craft)) return null;
		var result = craft.getInstance();
		if(Objects.isNull(result)) return null;
		return clazz.cast(result);
	}
	
	/**
	 * Get the managed object from context without exception.
	 */
	public<T> T silent(Class<T> clazz) {
		if(Objects.isNull(clazz)) return null;
		var name = clazz.getName();
		var craft = crafts.get(name);
		if(Objects.isNull(craft)) return null;
		var result = craft.getInstance();
		if(Objects.isNull(result)) return null;
		return clazz.cast(result);
	}
	
	/**
	 * Get the managed object without qualifier name.
	 */
	public<T> T get(Class<T> clazz) {
		return get(clazz.getName(), clazz);
	}
	
	public Map<String, Object> all(){
		if(Objects.isNull(crafts)) return Map.of();
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
		if(Objects.isNull(key)) return null;
		if(Objects.isNull(configs)) return null;
		return this.configs.get(key);
	}
	
	/**
	 * Cache the IOC context into  SERVLET context.
	 */
	public Context cache(ServletContext context) {
		if(Objects.isNull(context)) return this;
		context.setAttribute(NAME, this);
		return this;
	}
	
	/**
	 * For jakarta servlet api.
	 */
	public Context cache(jakarta.servlet.ServletContext context) {
		if(Objects.isNull(context)) return this;
		context.setAttribute(NAME, this);
		return this;
	}
	
	public void start() {
		if(!craftFactory.launched()) {
			this.craftFactory.launch();
		}
	}
	
	@Override
	public void close() {
		this.cleanup();
		this.crafts = null;
		this.configs = null;
		this.craftFactory = null;
		LOGGER.info("The whale context has been destroyed.");
	}
	
	private void cleanup() {
		if(Hotpot.isNull(crafts)) return;
		for(var craft : crafts.values()) {
			craft.destroy();
		}
		this.crafts.clear();
	}
}