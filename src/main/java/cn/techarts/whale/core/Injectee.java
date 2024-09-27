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
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import javax.inject.Named;
import cn.techarts.whale.Panic;
import cn.techarts.whale.Valued;
import cn.techarts.whale.util.Hotpot;

/**
 * The class describes an injection in a managed object.<p>
 * 1. REF: A reference to another craft.<br>
 * 2. KEY: A configuration key in the configs.properties.<br>
 * 3. VAL: An explicit value(String and built-in primitive types).<br>
 * 4. PROVIDER: Please refer to the {@link javax.inject.Provider}
 * @author rocwon@gmail.com
 */
public class Injectee {
	private int __t;
	//Null means REF
	private Type type;
	//Null means VAL
	private String name;
	private Object value;
	//Is it a Provider<T>?
	private boolean assembled;
	
	/**Create a REF object*/
	public static Injectee ref(String ref) {
		return new Injectee(ref, 1);
	}
	
	/**Create a KEY object*/
	public static Injectee key(String key, Class<?> t) {
		var result = new Injectee(key, 2);
		result.setType(t != null ? t : Object.class);
		return result;
	}
	
	/**Create a VAL object*/
	public static Injectee val(Object val) {
		var result = new Injectee(3);
		result.setValue(val);
		result.setType(val.getClass());
		return result;
	}
	
	/**Create an interface of Provider*/
	public static Injectee provider(Type t) {
		var result = new Injectee(4);
		result.setType(t);
		result.setName(t.getTypeName());
		return result;
	}
	
	Injectee(int __t) {
		this.__t = __t;
	}
	
	Injectee(String name, int __t) {
		this.__t = __t;
		this.setName(name);
	}
	
	public Injectee(Parameter p) {
		var named = p.getAnnotation(Named.class);
		var valued = p.getAnnotation(Valued.class);
		parseAnnotations(named, valued, p.getType());
	}
	
	public Injectee(Field f) {
		var named = f.getAnnotation(Named.class);
		var valued = f.getAnnotation(Valued.class);
		parseAnnotations(named, valued, f.getType());
	}
	
	private void  parseAnnotations(Named named, Valued valued, Class<?> clazz) {
		if(named != null && valued != null) {
			throw Panic.annotationConflicted();
		}
		if(valued != null) type = clazz;//KEY | VAL
		setName(clazz.getName(), named, valued);
	}
	
	
	private void setName(String t, Named n, Valued v) {
		this.name = t; //Default Name
		if(n == null && v == null) return; //REF
		if(v != null) {
			this.name = v.key();
			if(v.val().isBlank()) {
				this.setInjectType(2);
			}else {
				this.setInjectType(3);
				value = Hotpot.cast(type, v.val());
			}
		}else {
			var tmp = n.value();
			this.setInjectType(1); //REF
			if(!tmp.isBlank()) this.name = tmp;
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public String getName() {
		return name;
	}
	
	public boolean isREF() {
		return this.__t == 1;
	}
	
	public boolean isKEY() {
		return this.__t == 2;
	}
	
	public boolean isVAL() {
		return this.__t == 3;
	}
	
	/**is Provider*/
	public boolean isPRV() {
		return this.__t == 4;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		//To avoid duplicated setting 
		if(this.value == null) {
			this.value = value;
		}
	}
	
	public Type getType() {
		return type;
	}
	
	public void resetValue(Object value) {
		this.value = value;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public String getTypeName() {
		if(type == null) return null;
		return this.type.getTypeName();
	}
	
	public boolean completed() {
		return this.value != null;
	}

	public boolean isAssembled(boolean singleton) {
		return singleton && assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}
	
	public void setInjectType(int __t) {
		this.__t = __t;
	}
	
	public static Injectee of(String ref, String key, String val, String type) {
		var result = new Injectee(0);
		if(ref != null && !ref.isEmpty()) {
			result.setName(ref);
			result.setInjectType(1);
		}else if(key != null && !key.isEmpty()) {
			result.setName(key);
			result.setInjectType(2);
		}else if(val != null && !val.isEmpty()) {
			result.setValue(val);
			result.setInjectType(3);
		}
		if(type != null && !type.isEmpty()) {
			var ftn = Hotpot.fullTypeName(type);
			result.setType(Hotpot.forName(ftn));
		}
		return result;
	}
}