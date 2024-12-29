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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.techarts.whale.Panic;
import cn.techarts.whale.aop.DefaultHandler;
import cn.techarts.whale.util.Hotpot;

/**
 * The core concept of whale. <br>
 * It represents a managed object with meta-data in the container.
 * In Spring and other IOC frameworks, it's often referred to as "bean".
 * @author rocwon@gmail.com
 */
public class Craft {
	private String name;
	private String type;
	private Class<?> proxy;
	private Method onReady;
	private Object instance;
	private boolean singleton;
	private boolean assembled;
		
	/** Injected or default constructor*/
	private Constructor<?> constructor;
	
	/** Injected Constructor Arguments*/
	private Map<Integer, Injectee> arguments;
	
	/** Injected Fields(Values)*/
	private Map<Field, Injectee> properties;
	
	/** Injected Methods(setter or)*/
	private Map<Method, Injectee[]> methods;
	
	/**From XML Declaration*/
	public Craft(String type) {
		this.type = type;
		this.methods = new HashMap<>();
		this.arguments = new HashMap<>();
		this.properties = new HashMap<>();	
		var clazz = Hotpot.forName(type);
		//Mixed Injection: By Annotation
		this.resolveInjectedFields(clazz);
		this.resoveInjectedMethods(clazz);
	}
	
	/**From Annotation*/
	public Craft(Class<?> clazz, Analyzer analyzer) {
		this.proxy = analyzer.getProxy();
		this.name = analyzer.getQualifierName();
		this.singleton = analyzer.isSingleton();
		this.methods = new HashMap<>();
		this.arguments = new HashMap<>();
		this.properties = new HashMap<>();
		this.resolveInjectedFields(clazz);
		this.resoveInjectedMethods(clazz);
		this.resolveInjectedContructor(clazz);
	}
	
	/**Include an external singleton object(NON-JSR330) into DI container*/
	public Craft(String name, Object instance) {
		this.name = name;
		this.singleton = true;
		this.assembled = true;
		this.instance = instance;
	}
	
	/**
	 * Set dependent crafts (REF, KEY, VAL, PROVIDER) before assembling.
	 */
	public void inject(Map<String, Craft> crafts, Map<String, Craft> materials, Map<String, String> configs) {
		setConstructorDependences(crafts, materials, configs);
		setMethodsDependences(crafts, materials, configs);
		setPropertiesDependences(crafts, materials, configs);
	}
	
	//If the instance is not NULL, that means the craft is assembled successfully.
	private void setConstructorDependences(Map<String, Craft> crafts, Map<String, Craft> materials, Map<String, String> configs) {
		if(instance != null) return; //Set dependences completed
		for(int i = 0; i < arguments.size(); i++) {
			var arg = arguments.get(Integer.valueOf(i));
			if(arg.completed()) continue; //The value set already.
			if(arg.isKEY()) {
				arg.setValue(configs.get(arg.getName()));
			}else if(arg.isREF()){
				var craft = crafts.get(arg.getName());
				if(craft != null) arg.setValue(craft.getInstance());
			}else {	//Provider
				var craft = crafts.get(arg.getName());
				if(Objects.isNull(craft)) { //Circular dependence
					craft = materials.get(arg.getName());
				}else{
					var type = (Class<?>)arg.getType();
					arg.setValue(new ProviderImpl<>(type, craft));
				}
			}
		}
	}
		
	private void setMethodsDependences(Map<String, Craft> crafts, Map<String, Craft> materials, Map<String, String> configs) {
		for(var entity : methods.entrySet()) {
			var args = entity.getValue();
			if(args.length == 0) continue;
			for(var arg : args) {
				if(arg.completed()) continue;
				if(arg.isKEY()) {
					arg.setValue(configs.get(arg.getName()));
				}else if(arg.isREF()){
					var craft = crafts.get(arg.getName());
					if(craft != null) arg.setValue(craft.getInstance());
				}else {	//Provider
					var craft = crafts.get(arg.getName());
					if(Objects.isNull(craft)) { //Circular dependence
						craft = materials.get(arg.getName());
					}else{
						var type = (Class<?>)arg.getType();
						arg.setValue(new ProviderImpl<>(type, craft));
					}
				}
			}
		}
	}
	
	//Set REF and KEY (VAL set already)
	private void setPropertiesDependences(Map<String, Craft> crafts, Map<String, Craft> materials, Map<String, String> configs) {
		for(var entity : properties.entrySet()) {
			var field = entity.getValue();
			if(field.completed()) continue; //The value set already.
			if(field.isKEY()) { //Key here
				var v = configs.get(field.getName());
				if(Objects.isNull(v)) {
					throw Panic.configKeyMissing(field.getName());
				}
				field.setValue(Hotpot.cast(field.getType(), v));
			}else if(field.isREF()){
				var craft = crafts.get(field.getName());
				if(craft != null) field.setValue(craft.getInstance());
			}else { //Provider
				var craft = crafts.get(field.getName());
				if(Objects.isNull(craft)) {
					craft = materials.get(field.getName());
				}else {
					var type = (Class<?>)getGnericType(entity.getKey());
					field.setValue(new ProviderImpl<>(type, craft));
				}
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Object getInstance() {
		if(singleton) {
			return instance;
		}else {
			return construct()
				   .assemble()
				   .execute();
		}
	}
	
	public<T> T getInstance(Class<T> t){
		var result = getInstance();
		return t.cast(result);
	}
	
	//Constructor Parameters
	private Object[] toParameters() {
		var len = arguments.size();
		var result = new Object[len];
		for(int i = 0; i < len; i++) {
			var key = Integer.valueOf(i);
			var arg = arguments.get(key);
			if(Objects.isNull(arg)) return null;
			if(!arg.completed()) return null;
			result[i] = arg.getValue();
		}
		return result;
	}
	
	//Method parameters
	private Object[] toParameters(Method m) {
		var args = this.methods.get(m);
		if(args.length == 0) return new Object[0];
		var result = new Object[args.length];
		for(int i = 0; i < args.length; i++) {
			result[i] = args[i].getValue();
			if(Objects.isNull(result[i])) return null;
		}
		return result;
	}
	
	/**
	 * Execute the constructor but the fields and methods are not injected.
	 */
	public Craft construct() {
		if(instance != null && singleton) return this;
		try {
			if(isDefaultConstructor()) {
				instance = constructor.newInstance();
				instance = DefaultHandler.create(instance, proxy);
			}else {
				var params = toParameters();
				if(Objects.isNull(params)) return this; //Waiting...
				instance = constructor.newInstance(params);
				instance = DefaultHandler.create(instance, proxy);
			}
			//Support constructor and field injection mean time.
			this.assembled = this.properties.isEmpty();
			return this; //Just for supporting chain-style calling
		}catch(Exception e) {
			throw Panic.cannotInstance(this.name, e);
		}
	}
	
	/**
	 * Set value to injected fields.
	 */
	public Craft assemble() {
		if(this.assembled) return this;
		if(Objects.isNull(instance)) return this; //Waiting...
		this.assembled = true; //Suppose Completed
		for(var entry : properties.entrySet()) {
			var arg = entry.getValue();
			if(arg.isAssembled(singleton)) continue; //Used
			if(!arg.completed()) { //Waiting...
				this.assembled = false; continue;
			}			
			try {
				var f = entry.getKey();
				var a = f.canAccess(instance);
				if(!a) f.setAccessible(true);
				f.set(instance, arg.getValue());
				arg.setAssembled(true); //Ignored Next
				if(!a) f.setAccessible(false);
			}catch(Exception e) {
				throw Panic.cannotSetFieldValue(e);
			}
		}
		return this; //Just for chain-style calling
	}
	
	/**
	 * Execute injected methods.
	 */
	public Object execute() {
		if(Objects.isNull(instance)) return null; //Waiting...
		for(var entry : methods.entrySet()) {
			var params = toParameters(entry.getKey());
			if(Objects.isNull(params)) continue; //Waiting...
			try {
				entry.getKey().invoke(instance, params);
			}catch(Exception e) {
				var mn = entry.getKey().getName();
				throw Panic.cannotInvoke(name + "." + mn, e);
			}
		}
		this.init(); //If it has an initializer
		return this.instance; //Constructed, assembled, executed.
	}
	
	private Type getGnericType(Field f){
		var gt = f.getGenericType();
		if(!(gt instanceof ParameterizedType)) return null;
        var parameterizedType = (ParameterizedType) gt;
        return parameterizedType.getActualTypeArguments()[0];
	}
	
	private Type getGnericType(Parameter p){
		var gt = p.getParameterizedType();
		var parameterizedType = (ParameterizedType) gt;
        return parameterizedType.getActualTypeArguments()[0];
	}
	
	public boolean isDefaultConstructor() {
		return Objects.isNull(arguments) || arguments.isEmpty();
	}
	
	public Constructor<?> getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	public boolean isAssembled() {
		return assembled;
	}

	public void setAssembled(boolean assembled) {
		this.assembled = assembled;
	}
	
	public Craft withConstructor() {
		var clazz = Hotpot.forName(type);
		var cons = clazz.getConstructors();
		if(cons.length == 0) {
			throw Panic.noDefaultConstructor(clazz);
		}
		for(var c : cons) {
			var parameterMatched = true;
			var args = c.getParameters();
			if(args.length != arguments.size()) continue;
			
			for(int i = 0; i < args.length; i++) {
				var a = args[i].getType().getName();
				var e = arguments.get(i).getTypeName();
				if(!Hotpot.compareTypes(a, e)) {
					parameterMatched = false; break;
				}
			}
			if(parameterMatched) { 
				this.constructor = c; break;
			}
		}
		
		if(Objects.isNull(this.constructor)) {
			try { //Default and public constructor
				this.constructor = clazz.getConstructor();
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
		return this;
	}
	
	/**Important: You cannot use a Provider 
	 * as a parameter and call @link{get()} 
	 * method within the constructor immediately.
	*/
	private void resolveInjectedContructor(Class<?> clazz) {
		var cons = clazz.getConstructors();
		if(cons.length == 0) return;
		
		for(var c : cons) {
			if(!Analyzer.hasInjectAnnotation(c)) continue;
			this.constructor = c; //Cache it for new instance
			var args = c.getParameters();
			if(args.length == 0) break;
			
			for(int i = 0; i < args.length; i++) {
				if(!Analyzer.isProvider(args[i])) {
					var arg = new Injectee(args[i]);
					arguments.put(Integer.valueOf(i), arg);
				}else {
					var type = getGnericType(args[i]);
					var anns = args[i].getAnnotations();
					var arg = Injectee.of(type, anns);
					arguments.put(Integer.valueOf(i), arg);
				}
			}
			break; //Only ONE constructor can be injected
		}
		
		if(Objects.isNull(this.constructor)) {//explicitly?
			try { //Default and public constructor
				this.constructor = clazz.getConstructor();
			}catch(NoSuchMethodException | SecurityException es) {
				throw Panic.noDefaultConstructor(clazz, es);
			}
		}
	}
	
	private void resoveInjectedMethods(Class<?> clazz) {
		if(Objects.isNull(clazz)) return;
		var ms = clazz.getDeclaredMethods();
		if(ms != null && ms.length != 0) {
			for(var m : ms) {
				if(Analyzer.isInitializer(m)) {
					onReady = m; 
					continue;
				}				
				if(!Analyzer.hasInjectAnnotation(m)) continue;
				var args = m.getParameters();
				if(args.length == 0) {
					this.methods.put(m, new Injectee[0]);
				}else {
					var params = new Injectee[args.length];
					for(int i = 0; i < args.length; i++) {
						if(!Analyzer.isProvider(args[i])) {
							params[i] = new Injectee(args[i]);
						}else {
							var type = getGnericType(args[i]);
							var anns = args[i].getAnnotations();
							params[i] = Injectee.of(type, anns);
						}
						this.methods.put(m, params); 
					}
				}
			}
		}
		this.resoveInjectedMethods(clazz.getSuperclass());
	}
	
	private void resolveInjectedFields(Class<?> clazz) {
		if(Objects.isNull(clazz)) return;
		var fs = clazz.getDeclaredFields();
		if(fs != null && fs.length != 0) {
			for(var f : fs) {
				if(!Analyzer.hasInjectAnnotation(f)) continue;
				if(!Analyzer.isProvider(f)) {
					this.addProperty(f, new Injectee(f));
				}else {
					var type = getGnericType(f);
					var anns = f.getAnnotations();
					this.addProperty(f, Injectee.of(type, anns));
				}
			}
		}
		resolveInjectedFields(clazz.getSuperclass());
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setSingleton(String singleton) {
		this.singleton = Hotpot.toBoolean(singleton);
	}
	
	public void addArgument(int index, Injectee arg) {
		this.arguments.put(index, arg);
	}
	
	public void setInstance(Object instance) {
		this.instance = instance;
	}
	
	/**
	 * An interface
	 */
	public Class<?> getProxy() {
		return this.proxy;
	}
	
	public void addProperty(Field field, Injectee arg) {
		if(Hotpot.orNull(field, arg)) return;
		arg.setType(field.getType());
		this.properties.put(field, arg);
		var val = arg.getValue();
		if(Objects.isNull(val)) return; 
		arg.resetValue(Hotpot.cast(val, field.getType()));
	}
	
	public void addMethod(Method method, Injectee[] args) {
		if(Hotpot.orNull(method, args)) return;
		this.methods.put(method, args);
	}
	
	public void init() {
		if(!assembled) return;
		if(onReady == null) return;
		if(instance == null) return;
		try {
			onReady.invoke(instance);
		}catch(Exception e) {
			throw Panic.failed2Init(name, e);
		}
	}
	
	public void destroy() {
		if(Objects.isNull(instance)) return;
		if(instance instanceof AutoCloseable) {
			try {
				((AutoCloseable)instance).close();
				this.instance = null;
			}catch(Exception e) {
				throw Panic.failed2Close(name, e);
			}
		}
	}	
}