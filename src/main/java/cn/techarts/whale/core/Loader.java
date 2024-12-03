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

/**
 * @author rocwon@gmail.com
 */
public interface Loader {
	/**
	 * Load and register managed beans from a JAR file.
	 */
	public Loader load(String jar);
	
	/**
	 * Load and register managed beans from multiple JAR files.
	 */
	public Loader load(String[] jars);
	
	/**
	 * Parse the specified single XML file to register managed objects.
	 */
	public Loader parse(String xmlResource);
	
	/**
	 * Parse the specified multiple XML files to register managed objects.
	 */
	public Loader parse(String[] xmlResources);
	
	/**
	 * Scan the specified single class-path to register managed objects.
	 */
	public Loader scan(String classpath);
	
	/**
	 * Scan the specified multiple class-paths to register managed objects.
	 */
	public Loader scan(String[] classpaths);
}
