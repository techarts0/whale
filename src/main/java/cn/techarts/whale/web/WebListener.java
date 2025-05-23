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

package cn.techarts.whale.web;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletContextEvent;
//import jakarta.servlet.ServletContextListener;

import cn.techarts.whale.Context;
import cn.techarts.whale.Panic;
import cn.techarts.whale.util.Hotpot;

/**
 * <p>javax & jakarta</p>
 * The class allows developer to integrate the whale in a web project.
 * 
 * @author rocwon@gmail.com
 */
public class WebListener implements ServletContextListener {
	
	private static final Logger LOGGER = Hotpot.getLogger();
	
	@Override
	public void contextInitialized(ServletContextEvent arg) {
		var context = arg.getServletContext();
		var config = getResourcePath("config.properties");
		var ctx = Context.make(config).cache(context);
		var loader = ctx.getLoader();
		loader.scan(getRootClassPath())
			  .parse(getResourcePath("beans.xml"));
		
		ctx.start();
	}
	
	private String getRootClassPath() {
		var result = getClass().getResource("/");
		if(Hotpot.orNull(result, result.getPath())) {
			throw new Panic("Failed to find the root class path.");
		}
		return result.getPath();
	}
	
	private String getResourcePath(String resource) {
		var result = getClass().getResource("/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		result = getClass().getResource("/WEB-INF/".concat(resource));
		if(result != null && result.getPath() != null) return result.getPath();
		LOGGER.warning("Failed to find the resource: [" + resource + "]");
		return null; //Do not throw an exception to avoid application crash.
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		var ctx = arg0.getServletContext();
		var context = Context.from(ctx);
		if(context != null) context.close();
	}
	
	public<T> T get(ServletContext ctx, String id, Class<T> clzz){
		return Context.from(ctx).get(id, clzz);
	}
	
	public Object get(ServletContext ctx, String id){
		return Context.from(ctx).get(id);
	}
}