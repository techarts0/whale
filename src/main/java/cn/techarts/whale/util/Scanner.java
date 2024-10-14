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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;

/**
 * Scan the packages under the given base class-path or JAR file.
 * 
 * @author rocwon@gmail.com
 */
public class Scanner {
	
	public static List<String> scanClasses(File dest, int start){
		var result = new ArrayList<String>();
		var tmp = dest.listFiles(new ClassFilter());
		
		if(tmp != null && tmp.length != 0) {
			for(var file : tmp) {
				if(file.isFile()) {
					result.add(toClassName(file, start));
				}else {
					result.addAll(scanClasses(file, start));
				}
			}
		}
		return result;
	}
	
	private static String toClassName(File file, int start) {
		var path = file.getAbsolutePath();
		path = path.substring(start + 1);
		path = path.replaceAll("\\\\", ".");
		return path.replaceAll("/", ".").replace(".class", "");
	}
	
	/**List all class names in the JAR*/
	public static List<String> scanJar(String path) {
		var result = new ArrayList<String>();
		try(JarFile jar = new JarFile(new File(path))) {
	       var entries = jar.entries();
	        if(Objects.isNull(entries)) return result;
	        while(entries.hasMoreElements()) {
	            var entry = entries.nextElement();
	            var name = entry.getName();
	            if (!name.endsWith(".class")) continue;
	            name = name.substring(0, name.length() - 6);
	            result.add(name.replace('/', '.'));
	        }
	        return result;
		}catch(IOException e) {
			throw new RuntimeException("Failed to scan the jar file.", e);
		}
	}
}

class ClassFilter implements FileFilter
{
	public boolean accept(File file){
		if(Objects.isNull(file)) return false;
		if(file.isDirectory()) return true;
		var name =file.getName().toLowerCase();
		return name.endsWith(".class");
	}
}