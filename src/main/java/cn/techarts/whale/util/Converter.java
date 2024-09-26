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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Convert the specific type to another. For an incorrect input parameter,<p>
 * 1. returns 0 or 0.0 if the target type is a numeric, or<br>
 * 2. returns null if the target type is a date string. 
 * 
 * @author rocwon@gmail.com
 */
public final class Converter {
	public static int toInt( String arg){
		if(arg == null) return 0;
		try{ 
			return Integer.parseInt(arg);
		}catch( NumberFormatException e){ 
			return 0;
		}
	}
	
	public static int toInt( String arg, boolean abs){
		if(arg == null) return 0;
		try{ 
			int val = Integer.parseInt( arg);
			return abs && val < 0 ? Math.abs(val) : val;
		}catch( NumberFormatException e){ 
			return 0;
		}
	}
	
	public static int toInt( String arg, int defaultValue){
		if(arg == null) return defaultValue;
		try{ 
			return Integer.parseInt(arg);
		}catch( NumberFormatException e){ 
			return defaultValue;
		}
	}
	
	public static double toDouble( String arg){
		if(arg == null) return 0d;
		try{ 
			return Double.parseDouble(arg);
		}catch( NumberFormatException e){ 
			return 0d;
		}
	}
	
	public static long toLong( String arg){
		if(arg == null) return 0L;
		try{ 
			return Long.parseLong(arg);
		}catch(NumberFormatException e){ 
			return 0L;
		}
	}
	
	public static float toFloat(String arg){
		if(arg == null) return 0f;
		try{ 
			return Float.parseFloat(arg);
		}catch( NumberFormatException e){ 
			return 0f;
		}
	}
	
	/**
	 * A C-like style
	 */
	public static boolean toBoolean(int arg) {
		return arg != 0; //C-like style
	}
	
	/**
	 * @return Returns true if the parameter equals "1" or "true"
	 */
	public static boolean toBoolean(String arg) {
		if(arg == null) return false;
		var val = arg.trim().toLowerCase();
		return val.equals("1") || val.equals("true");
	}
	
	public static String toString(int arg) {
		return String.valueOf(arg);
	}
	
	public static String toString(long arg) {
		return Long.toString(arg);
	}
	
	public static String toString(float arg) {
		return Float.toString(arg);
	}
	
	public static String toString(double arg) {
		return Double.toString(arg);
	}
	
	
	/**
	 *@param arg The pattern of date string is yyyy-MM-dd or yyyy-MM-dd HH:mm:ss<p>
	 *It's same to the method {@link DateHelper.parse(String arg)} 
	 */
	public static Date toDate(String date) {
		if(date == null) return null;
		DateTimeFormatter formatter = null;
		var source = date.replace('/', '-');
		if(source.length() == 10) {
			formatter = DateTimeFormatter.ISO_LOCAL_DATE;
			return toDate(LocalDate.parse(source, formatter));
		}else { //Pattern: yyyy-MM-dd HH:mm:ss
			source = source.replace(' ', 'T');
			formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			return toDate(LocalDateTime.parse(source, formatter));
		}
	}
	
	private static Date toDate(LocalDate localDate) {
		if(localDate == null) return null;
		var zoneId = ZoneId.systemDefault();
		var zdt = localDate.atStartOfDay(zoneId);
		return Date.from(zdt.toInstant());
	}
	
	private static Date toDate(LocalDateTime localDateTime) {
		if(localDateTime == null) return null;
		var zoneId = ZoneId.systemDefault();
		var instant = localDateTime.atZone(zoneId).toInstant();
		return Date.from(instant);
	}
	
	public static<T> List<T> toList(T[] arg){
		if(arg == null) return null;
		var result = new ArrayList<T>();
		if(arg.length == 0) return result;
		for(var item : arg) {
			if(item != null) result.add(item);
		}
		return result;
	}
	
	public static<T> T to(Object arg, Class<T> clazz) {
		if(arg == null || clazz == null) return null;
		try {
			return clazz.cast(arg);
		}catch(ClassCastException e) {
			return null; //Cannot cast to the given type
		}
	}
	
	/**
	 * An alias of {@link Reflector.dump}
	 */
	public static Map<String, Object> toMap(Object arg){
		return Hotpot.dump(arg);
	}
	
	/**
	 * An alias of {@link Reflector.fill}
	 */
	public static void toBean(Object bean, Map<String, Object> data) {
		Hotpot.fill(bean, data);
	}
}