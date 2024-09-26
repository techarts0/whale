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
	public static final String NAME = "context.dragonfly.techarts.cn";
	private static final Logger LOGGER = Hotpot.getLogger();
	
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
	public static Context make(String config) {
		var container = new HashMap<String, Craft>(256);
		var configs = Hotpot.resolveProperties(config);
		return new Context(container, configs);
	}
	
	/**
	 * Construct a context with configuration.
	 */
	public static Context make(Map<String, String> configs) {
		return new Context(new HashMap<>(256), configs);
	}
	
	/**
	 * Create a bean factory to bind beans manually.<p>
	 * IMPORTANT: The factory will be reset if you recreate the factory object! 
	 */
	public Factory createFactory() {
		return new Factory(crafts, configs);
	}
	
	/**
	 * Retrieve the context from SERVLET context.(Web Application)
	 */
	public static Context from(ServletContext context) {
		var obj = context.getAttribute(NAME);
		if(obj == null) return null;
		if(!(obj instanceof Context)) return null;
		return (Context)obj;
	}
	
	Context(Map<String, Craft> container, Map<String, String> configs){
		this.configs = configs == null ? Map.of() : configs;
		this.crafts = container == null ? Map.of() : container;
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
	 * Get the managed object without qualifier name.
	 */
	public<T> T get(Class<T> clazz) {
		return get(clazz.getName(), clazz);
	}
	
	/**Export the configuration the container held.*/
	public String getConfig(String key) {
		if(key == null) return null;
		if(configs == null) return null;
		return this.configs.get(key);
	}
	
	/**
	 * Cache the IOC context into  SERVLET context.
	 */
	public Context cache(ServletContext context) {
		context.setAttribute(NAME, this);
		return this;
	}
	
	@Override
	public void close() {
		if(crafts == null) return;
		if(crafts.isEmpty()) return;
		for(var craft : crafts.values()) {
			var obj = craft.getInstance();
			if(obj == null) continue;
			if(obj instanceof AutoCloseable) {
				try {
					((AutoCloseable)obj).close();
				}catch(Exception e) {
					LOGGER.severe("Failed to close " + craft.getName() + ": " + e.getMessage());
				}
			}
		}
	}
}