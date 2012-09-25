/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class containing general helper methods.
 */
public class Util
{
	private final static Map<Class<?>, Class<?>> primitiveToBoxed;
	private final static Map<Class<?>, Class<?>> boxedToPrimitive;
	static
	{
		Map<Class<?>, Class<?>> ptb = new HashMap<Class<?>, Class<?>>();
		ptb.put(int.class, Integer.class);
		ptb.put(boolean.class, Boolean.class);
		ptb.put(long.class, Long.class);
		ptb.put(char.class, Character.class);
		ptb.put(byte.class, Byte.class);
		ptb.put(double.class, Double.class);
		ptb.put(float.class, Float.class);
		ptb.put(short.class, Short.class);
		primitiveToBoxed = Collections.unmodifiableMap(ptb);
		Map<Class<?>, Class<?>> btp = new HashMap<Class<?>, Class<?>>();
		for (Entry<Class<?>, Class<?>> entry : ptb.entrySet())
		{
			btp.put(entry.getValue(), entry.getKey());
		}
		boxedToPrimitive = Collections.unmodifiableMap(btp);
	}

	/**
	 * Convert the given primitive class (int.class, long.class, etc) to its
	 * boxed version (Integer.class, Long.class, etc). Returns null if the given
	 * class is not a primitive class.
	 * 
	 * @param primitiveClass
	 * @return Boxed version of the given primitive class
	 */
	public static Class<?> primitiveClassToBoxed(Class<?> primitiveClass)
	{
		return primitiveToBoxed.get(primitiveClass);
	}

	/**
	 * Convert the given boxed class (Integer.class, Long.class, etc) to its
	 * primitive version (int.class, long.class, etc). Returns null if the given
	 * class is not a boxed class.
	 * 
	 * @param boxedClass
	 * @return Primitive version of the given boxed class, or null
	 */
	public static Class<?> boxedClassToPrimitive(Class<?> boxedClass)
	{
		return boxedToPrimitive.get(boxedClass);
	}

	/**
	 * Split a string using the provided separator, then convert the split
	 * components to ints.
	 * 
	 * @param string
	 * @param separator
	 * @return Array of ints parsed from the string.
	 */
	public static int[] splitInts(String string, String separator)
	{
		String[] split = string.trim().split(separator);
		List<Integer> ints = new ArrayList<Integer>(split.length);
		for (String s : split)
		{
			try
			{
				ints.add(Integer.valueOf(s.trim()));
			}
			catch (Exception e)
			{
			}
		}
		int[] is = new int[ints.size()];
		for (int i = 0; i < is.length; i++)
		{
			is[i] = ints.get(i);
		}
		return is;
	}

	/**
	 * Concatenate an array of ints into a single string, separated by the
	 * provided separator.
	 * 
	 * @param array
	 * @param separator
	 * @return Concatenated ints as a string.
	 */
	public static String concatInts(int[] array, String separator)
	{
		if (array == null || array.length == 0)
			return ""; //$NON-NLS-1$
		if (separator == null)
			separator = ""; //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		for (int i : array)
		{
			sb.append(separator);
			sb.append(i);
		}
		return sb.substring(separator.length());
	}

	/**
	 * Is the given string empty (null, or of length 0)?
	 * 
	 * @param s
	 *            String to check if empty.
	 * @return True if the given string is empty.
	 */
	public static boolean isEmpty(String s)
	{
		return s == null || s.length() == 0;
	}
}
