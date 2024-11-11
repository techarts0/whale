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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Objects;

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
	private Type type;
	private String name;
	private Object value;
	private boolean assembled;
	
	public static final int NON = 0, REF = 1, KEY = 2, VAL = 3, PROVIDER = 4;
	
	/**Create a REF object*/
	public static Injectee ref(String ref) {
		return new Injectee(ref, REF);
	}
	
	/**Create a KEY object*/
	public static Injectee key(String key, Class<?> t) {
		var result = new Injectee(key, KEY);
		result.setType(t != null ? t : Object.class);
		return result;
	}
	
	/**Create a VAL object*/
	public static Injectee val(Object val) {
		var result = new Injectee(VAL);
		result.setValue(val);
		result.setType(val.getClass());
		return result;
	}
	
	/**Create an interface of Provider*/
	public static Injectee of(Type t, Annotation[] args) {
		var result = new Injectee(PROVIDER, t);
		var a = new Analyzer(args, 2, t.getTypeName());
		result.setName(a.getQualifierName());
		return result;
	}
	
	Injectee(int __t) {
		this.__t = __t;
	}
	
	Injectee(int __t, Type tpye) {
		this.__t = __t;
		this.type = tpye;
	}
	
	Injectee(String name, int __t) {
		this.__t = __t;
		this.setName(name);
	}
	
	public Injectee(Parameter p) {
		new Analyzer(p.getAnnotations(), 3).set(this, p.getType());
	}
	
	public Injectee(Field f) {
		new Analyzer(f.getAnnotations(), 3).set(this, f.getType());
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	public String getName() {
		return name;
	}
	
	public boolean isREF() {
		return this.__t == REF;
	}
	
	public boolean isKEY() {
		return this.__t == KEY;
	}
	
	public boolean isVAL() {
		return this.__t == VAL;
	}
	
	/**is Provider*/
	public boolean isPRV() {
		return this.__t == PROVIDER;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		if(Objects.isNull(this.value)) {
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
		if(Objects.isNull(type)) return null;
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
		var result = new Injectee(NON);
		
		if(type != null && !type.isEmpty()) {
			var ftn = Hotpot.fullTypeName(type);
			result.setType(Hotpot.forName(ftn));
		}
		
		if(ref != null && !ref.isEmpty()) {
			result.setName(ref);
			result.setInjectType(REF);
		}else if(key != null && !key.isEmpty()) {
			result.setName(key);
			result.setInjectType(KEY);
		}else if(val != null && !val.isEmpty()) {
			result.setInjectType(VAL);
			var t = result.getType(); //Maybe NULL
			result.setValue(Hotpot.cast(val, t));
		}
		
		return result;
	}
}