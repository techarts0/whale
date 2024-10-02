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

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import cn.techarts.whale.Valued;
import cn.techarts.whale.util.Hotpot;


/**
 * Analyze the annotations of the given class. 
 * It iterates each annotation to find the required metadata.<p>
 * 
 * The priority of qualifier name is: Priority: Named > Valued > Qualifier
 * 
 * @author rocwon@gmail.com
 */
public class Analyzer {
	private Named n = null;
	private Valued v = null;
	private Annotation q = null;
	private String defaultName = null;
	private boolean singleton = false;
	
	public boolean isManagedObject() {
		if(q != null) return true;
		if(singleton) return true;
		return n != null ? true : false;
	}
	
	public Analyzer(Annotation[] args, int phase) {
		if(args == null) return;
		for(var anno : args) {
			if(anno instanceof Singleton) {
				this.setSingleton(true);
			}
			if(anno instanceof Named) {
				this.n = (Named)anno; return;
			}
			if(phase == 3) { //Otherwise 2
				if(anno instanceof Valued) {
					this.v = (Valued)anno; return;
				}
			}
			var type = anno.annotationType();
			if(type.isAnnotationPresent(Qualifier.class)) {
				this.q = anno;
			}
		}
	}
	
	public Analyzer(Annotation[] args, int phase, String defaultName) {
		this(args, phase);
		this.defaultName = defaultName;
	}
	
	public boolean isSingleton() {
		return this.singleton;
	}
	
	private void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	public void set(Injectee arg, Class<?> clazz) {
		arg.setType(clazz);
		if(n != null) {
			arg.setInjectType(Injectee.REF);
			var tmp = n.value();
			arg.setName(tmp.isBlank() ? clazz.getName() : tmp);
			return;
		}
			
		if(v != null) {
			if(!v.key().isBlank()) {
				arg.setName(v.key());
				arg.setInjectType(Injectee.KEY);
			}else{ //VAL: Without Name
				arg.setInjectType(Injectee.VAL);
				arg.setValue(Hotpot.cast(clazz, v.val()));
			}
			return;
		}		
		arg.setInjectType(Injectee.REF); //Third
		arg.setName(q != null ? q.toString() : clazz.getName());
	}
	
	/**
	 * Priority: Named qualifier > Customized Qualifier > Class Name
	 */
	public String getQualifierName() {
		if(n != null && !n.value().isBlank()) return n.value();
		return q != null ? q.toString() : this.defaultName;
	}	
}