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
package au.gov.ga.earthsci.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class which contains methods for instantiating objects from strings
 * (when supported). Supported objects are boxed primitives, as well as objects
 * that have a single-parameter constructor which takes a {@link String}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StringInstantiable
{
	/**
	 * Is the given class able to be instantiated from a string?
	 * 
	 * @param c
	 *            Class to test
	 * @return True if the given class is able to be instantiated from a string.
	 */
	public static boolean isInstantiable(Class<?> c)
	{
		if (c == null)
		{
			return false;
		}
		if (c.isPrimitive())
		{
			return true;
		}
		if (c.isAssignableFrom(Character.class))
		{
			//the Character class doesn't have a String constructor
			return true;
		}
		try
		{
			//can the class be constructed from a String?
			c.getDeclaredConstructor(String.class); //throws exception if not found
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}

	/**
	 * Convert the given object to a string. If the object is of a type that
	 * doesn't support instantiation by this class (ie
	 * {@link #isInstantiable(Class)} returns false), this method will return
	 * null.
	 * 
	 * @param o
	 * @return Object converted to a string.
	 */
	public static String toString(Object o)
	{
		if (o == null)
		{
			return null;
		}
		if (!isInstantiable(o.getClass()))
		{
			return null;
		}
		return o.toString();
	}

	/**
	 * Convert the given string to an instance of the provided type. Will return
	 * null if the given type doesn't support instantiation by this class (ie
	 * {@link #isInstantiable(Class)} returns false).
	 * 
	 * @param s
	 * @param type
	 * @return String converted to an instance of the given type.
	 */
	@SuppressWarnings("unchecked")
	public static <E> E newInstance(String s, Class<E> type)
	{
		if (s == null)
		{
			return null;
		}
		if (type.isPrimitive())
		{
			type = (Class<E>) Util.primitiveClassToBoxed(type);
		}
		if (Util.boxedClassToPrimitive(type) != null && s.length() <= 0)
		{
			//don't try instantiating a boxed class with an empty string
			return null;
		}
		if (type.isAssignableFrom(Character.class) && s.length() > 0)
		{
			return type.cast(s.charAt(0));
		}
		try
		{
			Constructor<E> stringConstructor = type.getDeclaredConstructor(String.class);
			return stringConstructor.newInstance(s);
		}
		catch (InvocationTargetException e)
		{
			if (e.getCause() instanceof RuntimeException)
			{
				throw (RuntimeException) e.getCause();
			}
			throw new IllegalArgumentException(e.getCause());
		}
		catch (Exception e)
		{
		}
		return null;
	}
}
