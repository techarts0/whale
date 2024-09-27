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

import javax.inject.Provider;

/**
 * A default implementation of the interface {@link javax.inject.Provider<T>}.
 * 
 * @author rocwon@gmail.com
 */
public class ProviderImpl<T> implements Provider<T> {
	private Craft craft = null;
	private Class<T> clazz = null;
	
	
	public ProviderImpl(){}
	
	public ProviderImpl(Class<T> clazz, Craft bean) {
		this.craft = bean;
		this.clazz = clazz;
	}
	
	public boolean verify() {
		if(craft == null) return false;
		return craft.isAssembled();
	}
	
	@Override
	public T get() {
		if(craft == null) return null;
		if(!craft.isAssembled()) return null;
		return clazz.cast(craft.getInstance());
	}
}