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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.techarts.whale.util.Hotpot;

/**
 * A node of XML(props, methods and args).
 * 
 * @author rocwon@gmail.com
 */
public class XmlBean {
	
	
	//TODO XML DOES NOT SUPPORT PROVIDER AND BIND
	public XmlBean() {
	}
	
	public Craft toCraft(Node bean) {
		if(bean.getNodeType() != Node.ELEMENT_NODE) return null;
		var craft = (org.w3c.dom.Element)bean;
		var result = new Craft(craft.getAttribute("type"));
		result.setName(craft.getAttribute("id"));
		result.setSingleton(craft.getAttribute("singleton"));
		parseArgs(craft.getElementsByTagName("args"), result);
		parseProps(craft.getElementsByTagName("props"), result);
		parseMethods(craft.getElementsByTagName("methods"), result);
		return result.withConstructor();
	}
	
	private void parseArgs(NodeList nodes, Craft result) {
		if(this.notOnly(nodes)) return;
		var first = (org.w3c.dom.Element)nodes.item(0);
		var args = first.getElementsByTagName("arg");
		if(this.isNull(args)) return;
		for(int i = 0; i < args.getLength(); i++) {
			var arg = args.item(i);
			if(arg.getNodeType() != Node.ELEMENT_NODE) continue;
			var injector = xmlNode2Injectee((Element)arg);
			result.addArgument(i, injector);
		}
	}
	
	private void parseProps(NodeList nodes, Craft result) {
		if(this.notOnly(nodes)) return;
		var first = (org.w3c.dom.Element)nodes.item(0);
		var props = first.getElementsByTagName("prop");
		if(this.isNull(props)) return;
		var fields = new HashMap<String, Field>();
		getFields(fields, Hotpot.forName(result.getType()));
		for(int i = 0; i < props.getLength(); i++) {
			var prop = props.item(i);
			if(prop.getNodeType() != Node.ELEMENT_NODE) continue;
			var tmp = (Element)prop;
			var name = tmp.getAttribute("name");
			var injector = xmlNode2Injectee(tmp);
			result.addProperty(fields.get(name), injector);
		}
	}
	
	private void parseMethods(NodeList nodes, Craft result) {
		if(this.notOnly(nodes)) return;
		var first = (org.w3c.dom.Element)nodes.item(0);
		var methods = first.getElementsByTagName("method");
		if(this.isNull(methods)) return;
		var funs = new HashMap<String, Method>();
		getMethods(funs, Hotpot.forName(result.getType()));
		for(int i = 0; i < methods.getLength(); i++) {
			var fun = methods.item(i); //A method
			if(fun.getNodeType() != Node.ELEMENT_NODE) continue;
			var tmp = (Element)fun;
			var name = tmp.getAttribute("name");
			var injectees = xmlNode2Injectees(tmp);
			result.addMethod(funs.get(name), injectees);
		}
	}
	
	private Injectee xmlNode2Injectee(Element node) {
		var ref = node.getAttribute("ref");
		var key = node.getAttribute("key");
		var val = node.getAttribute("val");
		var type = node.getAttribute("type");
		return Injectee.of(ref, key, val, type);
	}
	
	private Injectee[] xmlNode2Injectees(Element node) {
		var args = node.getElementsByTagName("arg");
		if(this.isNull(args)) return null;
		var result = new Injectee[args.getLength()];
		for(int i = 0; i < args.getLength(); i++) {
			var arg = args.item(i);
			if(arg.getNodeType() != Node.ELEMENT_NODE) continue;
			result[i] = xmlNode2Injectee((Element)arg);
		}
		return result;
	}
	
	private void getFields(Map<String, Field> result, Class<?> clazz) {
		if(Objects.isNull(clazz)) return; //Without super class
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				result.put(f.getName(), f);
			}
		}
		getFields(result, clazz.getSuperclass());
	}	
	
	private void getMethods(Map<String, Method> result, Class<?> clazz) {
		if(Objects.isNull(clazz)) return; //Without super class
		var ms = clazz.getDeclaredMethods();
		if(ms != null && ms.length != 0) {
			for(var m : ms) {
				result.put(m.getName(), m);
			}
		}
		getMethods(result, clazz.getSuperclass());
	}
	
	private boolean isNull(NodeList arg) {
		return arg == null || arg.getLength() == 0;
	}
	
	private boolean notOnly(NodeList arg) {
		return arg == null || arg.getLength() != 1;
	}
}