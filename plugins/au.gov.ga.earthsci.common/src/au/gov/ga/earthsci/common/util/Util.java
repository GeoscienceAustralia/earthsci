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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	private Util()
	{
	}

	private static final Map<Class<?>, Class<?>> primitiveToBoxed;
	private static final Map<Class<?>, Class<?>> boxedToPrimitive;
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
		{
			return ""; //$NON-NLS-1$
		}
		if (separator == null)
		{
			separator = ""; //$NON-NLS-1$
		}
		StringBuilder sb = new StringBuilder();
		for (int i : array)
		{
			sb.append(separator);
			sb.append(i);
		}
		return sb.substring(separator.length());
	}

	/**
	 * Concatenate an array of objects in a single string, separated by the
	 * provided separator.
	 * <p/>
	 * If an object is <code>null</code>, the provided {@code nullValue} will be
	 * used.
	 * 
	 * @param array
	 *            The array of values to concatenate
	 * @param separator
	 *            The separator to use when concatenating
	 * @param nullValue
	 *            The value to use in the case of a null element
	 * 
	 * @return The concatenated string
	 */
	public static String concat(Object[] array, String separator, String nullValue)
	{
		if (array == null || array.length == 0)
		{
			return ""; //$NON-NLS-1$
		}
		if (separator == null)
		{
			separator = ""; //$NON-NLS-1$
		}
		StringBuilder sb = new StringBuilder();
		for (Object o : array)
		{
			sb.append(separator);
			sb.append(o == null ? nullValue : o.toString());
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

	/**
	 * Returns a blank string if the given string is null, otherwise returns the
	 * string.
	 * 
	 * @param s
	 * @return Blank string if <code>s</code> is null, otherwise <code>s</code>
	 */
	public static String blankNullString(String s)
	{
		return s == null ? "" : s; //$NON-NLS-1$
	}

	/**
	 * Remove all whitespace from the given string
	 * 
	 * @param s
	 * @return The input string with all whitespace removed
	 */
	public static String removeWhitespace(String s)
	{
		return s == null ? null : s.replaceAll("\\s*", ""); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Perform a null-safe equality test on the two provided objects
	 * 
	 * @return <code>true</code> if both objects are equal according to
	 *         {@link Object#equals(Object)}, or if both objects are
	 *         <code>null</code>.
	 */
	public static boolean nullSafeEquals(Object o1, Object o2)
	{
		if (o1 == o2)
		{
			return true;
		}
		if ((o1 == null && o2 != null) || (o1 != null && o2 == null))
		{
			return false;
		}
		return o1.equals(o2);
	}

	/**
	 * Call the given named property's setter on the object with an Object
	 * represented by the propertyValue string. The property's setter's
	 * parameter count must be 1, and the parameter class must be
	 * {@link StringInstantiable#isInstantiable(Class)}.
	 * 
	 * @param o
	 *            Object on which to call the setter
	 * @param propertyName
	 *            Name of the property to set
	 * @param propertyValue
	 *            {@link StringInstantiable} representation of the property
	 *            value
	 * @return True if the setter method was found and invoked without error,
	 *         false otherwise
	 * @throws InvocationTargetException
	 *             If an exception was raised while invoking the setter
	 */
	public static boolean setPropertyOn(Object o, String propertyName, String propertyValue)
			throws InvocationTargetException
	{
		if (isEmpty(propertyName))
		{
			throw new IllegalArgumentException("Property name is empty"); //$NON-NLS-1$
		}

		String methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1); //$NON-NLS-1$
		Method[] methods = o.getClass().getMethods();
		for (Method method : methods)
		{
			if (method.getName().equals(methodName) && method.getParameterTypes().length == 1)
			{
				Class<?> type = method.getParameterTypes()[0];
				if (StringInstantiable.isInstantiable(type))
				{
					Object value = StringInstantiable.newInstance(propertyValue, type);
					if (value != null)
					{
						try
						{
							method.invoke(o, value);
							return true;
						}
						catch (InvocationTargetException e)
						{
							throw e;
						}
						catch (Exception e)
						{
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Save the given InputStream to a temporary file. Uses the
	 * {@link File#createTempFile(String, String)} method to generate the file.
	 * 
	 * @param is
	 *            InputStream to save
	 * @param prefix
	 *            Prefix to use for the filename; must be at least 3 characters
	 * @param suffix
	 *            Suffix to use for the filename; if null, <code>.tmp</code> is
	 *            used
	 * @return File containing the contents of the InputStream
	 * @throws IOException
	 *             If writing fails
	 */
	public static File writeInputStreamToTemporaryFile(InputStream is, String prefix, String suffix) throws IOException
	{
		File file = File.createTempFile(prefix, suffix);
		file.deleteOnExit();
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(file);
			writeInputStreamToOutputStream(is, fos);
		}
		finally
		{
			if (fos != null)
			{
				fos.close();
			}
		}
		return file;
	}

	/**
	 * Write the given InputStream to the given OutputStream, until no bytes are
	 * left in the InputStream. Uses a buffer size of 8096 bytes.
	 * 
	 * @param is
	 *            InputStream to read from
	 * @param os
	 *            OutputStream to write to
	 * @throws IOException
	 *             If writing fails
	 */
	public static void writeInputStreamToOutputStream(InputStream is, OutputStream os) throws IOException
	{
		writeInputStreamToOutputStream(is, os, 8096);
	}

	/**
	 * Write the given InputStream to the given OutputStream, until no bytes are
	 * left in the InputStream.
	 * 
	 * @param is
	 *            InputStream to read from
	 * @param os
	 *            OutputStream to write to
	 * @param bufferSize
	 *            Size of the intermediate buffer
	 * @throws IOException
	 *             If writing fails
	 */
	public static void writeInputStreamToOutputStream(InputStream is, OutputStream os, int bufferSize)
			throws IOException
	{
		byte[] buffer = new byte[bufferSize];
		int len;
		while ((len = is.read(buffer)) >= 0)
		{
			os.write(buffer, 0, len);
		}
	}

	/**
	 * Read the file extension from the given string. This is defined as the
	 * string including and after the last '.' dot (eg
	 * <code>temp/file.txt</code> = <code>.txt</code>).
	 * <p/>
	 * Returns null for a null path, or an empty string if no extension can be
	 * found or the extension contains non-word characters [a-zA-Z_0-9].
	 * 
	 * 
	 * @param path
	 *            Path to determine the file extension for
	 * @return File extension for path
	 */
	public static String getExtension(String path)
	{
		if (path == null)
		{
			return null;
		}
		int lastIndexOfDot = path.lastIndexOf('.');
		int lastIndexOfSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		if (lastIndexOfDot > lastIndexOfSlash)
		{
			String extension = path.substring(lastIndexOfDot);
			if (extension.matches("\\w+")) //$NON-NLS-1$
			{
				return extension;
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Read the given stream until EOF, and return a string.
	 * 
	 * @param stream
	 *            Stream to read
	 * @param charsetName
	 *            Charset to read the stream in
	 * @return String read from stream
	 * @throws IOException
	 *             If an IO error occurs during read
	 */
	public static String readStreamToString(InputStream stream, String charsetName) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[8192];
		int read;
		while ((read = stream.read(buffer)) >= 0)
		{
			String s = new String(buffer, 0, read, charsetName);
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Search through the text, and replace all instances of a character with a
	 * string.
	 * 
	 * @param text
	 *            Text to replace characters within
	 * @param c
	 *            Character to replace
	 * @param s
	 *            String to replace characters with
	 * @return Text with all <code>c</code> characters replaced with
	 *         <code>s</code>
	 */
	public static String replace(String text, char c, String s)
	{
		int previous = 0;
		int current = text.indexOf(c, previous);
		if (current == -1)
		{
			return text;
		}

		StringBuilder sb = new StringBuilder();
		while (current > -1)
		{
			sb.append(text.substring(previous, current));
			sb.append(s);
			previous = current + 1;
			current = text.indexOf(c, previous);
		}
		sb.append(text.substring(previous));

		return sb.toString();
	}
}
