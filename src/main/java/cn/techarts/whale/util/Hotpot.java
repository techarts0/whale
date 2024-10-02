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

package cn.techarts.whale.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import cn.techarts.whale.Panic;

/**
 * Various kinds of UN-classifiable helper methods.
 * 
 * @author rocwon@gmail.com
 */
public final class Hotpot {
	
	public static Object cast(Object v, Type t) {
		if(t == null) return v;
		if(!(v instanceof String)) {
			return v;
		}
		return cast(t, (String)v);
	}
	
	/**
	 * Excludes interface, annotation, anonymous and abstract class.
	 */
	public static boolean newable(Class<?> clazz) {
		if(clazz.isInterface()) return false;
		if(clazz.isAnnotation()) return false;
		if(clazz.isAnonymousClass()) return false;
		return !Modifier.isAbstract(clazz.getModifiers());
	}
	
	public static Object cast(Type t, String v) {
		var name = t.getTypeName();
		try {
			switch(name) {
				case "java.lang.String":
					return v;
				case "String":
					return v;
				case "java.lang.Integer":
					return Integer.parseInt(v);
				case "java.lang.Float":
					return Float.parseFloat(v);
				case "java.lang.Double":
					return Double.parseDouble(v);
				case "java.lang.Long":
					return Long.parseLong(v);
				case "java.lang.Boolean":
					return Boolean.parseBoolean(v);
				case "java.lang.Short":
					return Short.parseShort(v);
				case "java.lang.Byte":
					return Byte.parseByte(v);
				case "int":
					return Integer.parseInt(v);
				case "float":
					return Float.parseFloat(v);
				case "double":
					return Double.parseDouble(v);
				case "long":
					return Long.parseLong(v);
				case "boolean":
					return Boolean.parseBoolean(v);
				case "short":
					return Short.parseShort(v);
				case "byte":
					return Byte.parseByte(v);
				default:
					throw Panic.unsupportedType(name);
			}
		}catch( NumberFormatException e) {
			throw Panic.typeConvertError(name, v, e);
		}
	}
	
	private static final Map<String, Integer> PRIMITIVES = new HashMap<>() {
		private static final long serialVersionUID = 1L;

	{
		put("java.lang.Integer",   1);    put("int",    1);
		put("java.lang.Long",      2);    put("long",   2);
		put("java.lang.Float",     3);    put("float",  3);
		put("java.lang.Short",     4);    put("short",  4);
		put("java.lang.Boolean",   5);    put("boolean",5);
		put("java.lang.Double",    6);    put("double", 6);
		put("java.lang.Byte",      7);    put("byte",   7);
		put("java.lang.Character", 8);    put("char",   8);
		put("java.lang.String",    9);    put("String", 9);
	}};
	
	private static final Map<String, String> SHORTNAMES = new HashMap<>() {
		private static final long serialVersionUID = 1L;

	{
		put("int", "java.lang.Integer"); 	put("long", "java.lang.Long");
		put("float", "java.lang.Float"); 	put("short", "java.lang.Short");
		put("boolean", "java.lang.Boolean");put("double", "java.lang.Double");
		put("byte", "java.lang.Byte");		put("char", "java.lang.Character");
		put("string", "java.lang.String");
	}};
			
	public static String fullTypeName(String type) {
		var result = SHORTNAMES.get(type.toLowerCase());
		return result != null ? result : type;
	}
	
	/**
	 * @return Returns true if the parameter equals "1" or "true"
	 */
	public static boolean toBoolean(String arg) {
		if(arg == null) return false;
		var val = arg.trim().toLowerCase();
		return val.equals("1") || val.equals("true");
	}
	
	public static boolean isPrimitive(Class<?> clazz) {
		var name = clazz.getName();
		return PRIMITIVES.containsKey(name);
	}
	
	public static boolean compareTypes(String actual, String expect) {
		if(expect.equals(actual)) return true;
		return PRIMITIVES.get(actual) == PRIMITIVES.get(expect);
	}
	
	private static String toFieldName(String method) {
		var chars = method.toCharArray();
		var idx = method.startsWith("is") ? 2 : 3;
		chars[idx] = (char)(chars[idx] + 32); //To lower-case
		return new String(slice(chars, idx, 100));
	}
	
	private static char[] slice(char[] arg, int start, int end) {
		if(arg == null || end < start) return null;
		var endIndex = getEndIndex(end, arg.length);
		var result = new char[endIndex - start + 1];
		System.arraycopy(arg, start, result, 0, result.length);
		return result;
	}
	
	private static int getEndIndex(int end, int length) {
		return end < length ? end : length - 1;
	}
	
	private static boolean isGetter(String name) {
		if(name == null) return false;
		if(name.startsWith("is")) return true;
		return name.startsWith("get");
	}
	
	public static Class<?> forName(String clazz){
		try {
			return Class.forName(clazz);
		}catch(ClassNotFoundException e){
			throw Panic.classNotFound(clazz, e);
		}
	}
	
	/**
	 * Map to Bean
	 */
	public static void fill(Object target, Map<String, Object> data) {
		if(target == null || data == null) return;
		try {
			var clazz = target.getClass();
			var methods = clazz.getMethods();
			if(methods == null) return;
			for(var m : methods) {
				var name = m.getName();
				if(!name.startsWith("set")) continue;
				if(m.getParameterCount() != 1) continue;
				var param = data.get(toFieldName(name));
				if(param != null) m.invoke(target, param);
			}
		}catch(Exception e) {
			throw new RuntimeException("Failed to fill values to the bean", e);
		}	
	}
	
	/**
	 * Bean to Map
	 */
	public static Map<String, Object> dump(Object target) {
		if(target == null) return Map.of();
		try {
			var clazz = target.getClass();
			var methods = clazz.getMethods();
			if(methods == null || methods.length == 0) return Map.of();
			var getters = new ArrayList<String>();
			for(var method : methods) {
				var name = method.getName();
				if(!isGetter(name)) continue;
				if(method.getParameterCount() > 0) continue;
				getters.add(name); //A legal getter method
			}
			if(getters.isEmpty()) return Map.of();
			var result = new HashMap<String, Object>(32);
			for(var getter : getters) {
				var m = clazz.getMethod(getter);
				if(m != null) {
					var f = toFieldName(getter);
					result.put(f, m.invoke(target));
				}
			}
			return result;
		}catch(Exception e) {
			throw new RuntimeException("Failed to dump the values to map", e);
		}
	}
	
	public static Logger getLogger() {
		return Logger.getGlobal();
	}
	
	public static Logger getLogger(String name) {
		return Logger.getLogger(name);
	}
	
	/**
	 * Properties configuration
	 */
	public static Map<String, String> resolveProperties(String file) {
		var config = new Properties();
		var result = new HashMap<String, String>(64);
		try(var in = new FileInputStream(file)) {
			config.load(in);
			for(var key : config.stringPropertyNames()) {
				result.put(key, config.getProperty(key));
			}
			return result;
		}catch(IOException e) {
			throw new Panic("Failed to load config [" + file + "]", e);
		}
	}	
	
	public static String getFirst(String[] statements) {
		if(statements == null) return null;
		if(statements.length == 0) return null;
		return statements[0]; //Note: maybe null here
	}
	
	private static final String P = "^\\s*[a-zA-Z\\s][a-zA-Z0-9_.-]*\\s*=\\s*.*$";
	
	/**
	 * INI-Liked configuration
	 */
	public static Map<String, String> resolveConfiguration(String path){
		boolean multiLines = false;
		String line = null, sentence = null;
		var result = new HashMap<String, String>(512);
		try(var reader = new BufferedReader(new FileReader(path))){
			while((line = reader.readLine()) != null) {
				line = line.stripTrailing();
				if(line.isBlank()) continue;
				if(line.startsWith("#")) continue;
				if(multiLines && sentence != null) {
					var end = sentence.length() - 2;
					sentence = sentence.substring(0, end).concat(line);
				}
				if(!multiLines && line.matches(P)) sentence = line;
				multiLines = line.endsWith("\\\\"); //Current Line
				if(sentence == null || multiLines) continue;
				int i = sentence.indexOf('=');
				var k = sentence.substring(0, i);
				var v = sentence.substring(i + 1);
				if(v == null || v.length() == 0) continue;
				result.put(k.trim(), v.trim());
				sentence = null; //Reset the variable 
			}
			return result;
		}catch(IOException e){
			throw new RuntimeException("Fail to load the ini file", e);
		}
	}
}