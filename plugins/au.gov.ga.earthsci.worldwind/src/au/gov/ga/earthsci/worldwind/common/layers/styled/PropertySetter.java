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
package au.gov.ga.earthsci.worldwind.common.layers.styled;

import static au.gov.ga.earthsci.worldwind.common.util.Util.*;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generalised property setter. Defines a collection of properties that are set
 * on an object using reflection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PropertySetter
{
	protected final Map<String, String> properties = new HashMap<String, String>();
	protected final Map<String, String[]> typeOverrides = new HashMap<String, String[]>();

	/**
	 * Add a property that this setter will set.
	 * 
	 * @param property
	 *            Property name
	 * @param value
	 *            Value to set the property to (can replace with attribute
	 *            values by using a %attributeName% placeholder)
	 * @param typeOverride
	 *            Type overrides to use when setting the property
	 */
	public void addProperty(String property, String value, String... typeOverrides)
	{
		properties.put(property, value);
		if (typeOverrides != null && typeOverrides.length > 0)
		{
			this.typeOverrides.put(property, typeOverrides);
		}
	}

	/**
	 * Add a property that this setter will set.
	 * 
	 * @param property
	 *            Property name
	 * @param value
	 *            Value to set the property to (can replace with attribute
	 *            values by using a %attributeName% placeholder)
	 * @param typeOverride
	 *            Type to use when setting the property. Pipe separated lists
	 *            supported.
	 */
	public void addProperty(String property, String value, String typeOverrides)
	{
		addProperty(property, value, splitPipeSeparatedString(typeOverrides));
	}

	/**
	 * Set the objects properties to the values in this setter. Iterates through
	 * each of the properties in this setter, searches for a matching setter
	 * method for the property, and if found, calls the setter with this
	 * object's property value. Can also insert values from the attributes
	 * themselves, by using the %attributeName% placeholder in the value string.
	 * 
	 * @param context
	 *            Layer's context url
	 * @param attributeValues
	 *            Attribute values
	 * @param objects
	 *            Objects to search for matching setter properties, using
	 *            reflection
	 */
	public void setPropertiesFromAttributes(URL context, AVList attributeValues, Object... objects)
	{
		Map<String, Method> methods = new HashMap<String, Method>();
		Map<Method, Object> methodToObject = new HashMap<Method, Object>();

		//create a list of the methods in the objects
		for (Object object : objects)
		{
			for (Method method : object.getClass().getMethods())
			{
				methods.put(method.getName(), method);
				methodToObject.put(method, object);
			}
		}

		//for each of the properties in this setter
		for (Entry<String, String> entry : properties.entrySet())
		{
			//search for the setter method for this property
			String property = entry.getKey();
			String methodName = constructSetterName(property);
			if (!methods.containsKey(methodName))
			{
				String message = "Could not find setter method '" + methodName + "' in class: ";
				for (Object object : objects)
				{
					message += object.getClass() + ", ";
				}
				message = message.substring(0, message.length() - 2);

				Logging.logger().warning(message);
				continue;
			}

			//find out the method's parameters
			Method setter = methods.get(methodName);
			Object object = methodToObject.get(setter);
			Class<?>[] parameters = setter.getParameterTypes();

			//get the string value to pass to the method
			String stringValue = entry.getValue();
			stringValue = replaceVariablesWithAttributeValues(stringValue, attributeValues);

			String[] paramValueStrings = splitPipeSeparatedString(stringValue);

			if (parameters.length != paramValueStrings.length)
			{
				String message =
						"Setter method '" + methodName + "' in class " + object.getClass() + " doesn't take "
								+ paramValueStrings.length + " parameter(s)";
				Logging.logger().severe(message);
				// Continue on incase this is an overloaded method
				continue;
			}

			Object[] parameterValues = new Object[paramValueStrings.length];
			String[] typeOverrides = getTypeOverridesForProperty(property, parameterValues.length);

			// Convert each parameter value string into a parameter
			for (int i = 0; i < paramValueStrings.length; i++)
			{
				//find out the type to pass to the method
				Class<?> parameterType = parameters[i];
				Class<?> type = parameterType;

				//check if the type has been overridden (useful if the type above is just 'Object')
				String typeOverride = typeOverrides[i];
				if (!isBlank(typeOverride))
				{
					type = convertTypeToClass(typeOverride);
					if (type == null)
					{
						String message = "Could not find class for type " + type;
						Logging.logger().severe(message);
						throw new IllegalArgumentException(message);
					}
					else if (!parameterType.isAssignableFrom(type))
					{
						String message =
								"Setter method '" + methodName + "' in class " + object.getClass() + " parameter type "
										+ parameterType + " not assignable from type " + type;
						Logging.logger().severe(message);
						throw new IllegalArgumentException(message);
					}
				}

				//convert the string value to a valid type
				Object value = convertStringToType(context, paramValueStrings[i], type);
				if (value == null)
				{
					String message = "Error converting '" + paramValueStrings[i] + "' to type " + type;
					Logging.logger().severe(message);
					throw new IllegalArgumentException(message);
				}

				parameterValues[i] = value;
			}

			//invoke the setter with the value
			try
			{
				setter.invoke(object, parameterValues);
			}
			catch (Exception e)
			{
				String message = "Error invoking '" + methodName + "' in class " + object.getClass() + ": " + e;
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message, e);
			}
		}
	}

	/**
	 * @return the type overrides for the provided property, populated to ensure
	 *         there are the correct number of overrides for the parameters of
	 *         the property.
	 */
	private String[] getTypeOverridesForProperty(String property, int numberOfParameters)
	{
		String[] result = typeOverrides.get(property);
		if (result == null)
		{
			return new String[numberOfParameters];
		}
		if (result.length == numberOfParameters)
		{
			return result;
		}

		String[] propertyOverrides = typeOverrides.get(property);
		result = new String[numberOfParameters];
		for (int i = 0; i < result.length; i++)
		{
			if (i < propertyOverrides.length)
			{
				result[i] = propertyOverrides[i];
			}
			else
			{
				result[i] = null;
			}
		}
		return result;
	}

	private static String[] splitPipeSeparatedString(String stringValue)
	{
		// Split on '|' and trim whitespace at the same time
		return stringValue.trim().split("[ \t]*[|][ \t]*");
	}

	private static String constructSetterName(String property)
	{
		return "set" + capitalizeFirstLetter(property);
	}

	/**
	 * Replaces attribute placeholders in a string with the attribute value
	 * 
	 * @param string
	 *            String to replace placeholders in
	 * @param attributesValues
	 *            Attribute values
	 * @return Replaced string
	 */
	protected static String replaceVariablesWithAttributeValues(String string, AVList attributesValues)
	{
		if (attributesValues == null)
			return string;

		Pattern pattern = Pattern.compile("%[^%]+%");
		Matcher matcher = pattern.matcher(string);
		StringBuffer replacement = new StringBuffer();
		int start = 0;
		while (matcher.find(start))
		{
			replacement.append(string.substring(start, matcher.start()));

			String attribute = matcher.group();
			attribute = attribute.substring(1, attribute.length() - 1);

			if (!attributesValues.hasKey(attribute))
			{
				String message = "Could not find attribute '" + attribute + "'";
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			String value = attributesValues.getValue(attribute).toString();
			replacement.append(value);

			start = matcher.end();
		}

		replacement.append(string.substring(start));
		return replacement.toString();
	}

	/**
	 * Convert a type string to a class
	 * 
	 * @param type
	 * @return Class represented by the type string
	 */
	protected static Class<?> convertTypeToClass(String type)
	{
		if ("String".equalsIgnoreCase(type))
			return String.class;
		if ("Integer".equalsIgnoreCase(type))
			return Integer.class;
		if ("Float".equalsIgnoreCase(type))
			return Float.class;
		if ("Long".equalsIgnoreCase(type))
			return Long.class;
		if ("Double".equalsIgnoreCase(type))
			return Double.class;
		if ("Character".equalsIgnoreCase(type))
			return Character.class;
		if ("Byte".equalsIgnoreCase(type))
			return Byte.class;
		if ("URL".equalsIgnoreCase(type))
			return URL.class;
		if ("File".equalsIgnoreCase(type))
			return File.class;
		if ("Color".equalsIgnoreCase(type))
			return Color.class;
		if ("Insets".equalsIgnoreCase(type))
			return Insets.class;
		if ("Dimension".equalsIgnoreCase(type))
			return Dimension.class;
		if ("Point".equalsIgnoreCase(type))
			return Point.class;
		if ("Font".equalsIgnoreCase(type))
			return Font.class;
		if ("Material".equalsIgnoreCase(type))
			return Material.class;
		if ("Boolean".equalsIgnoreCase(type))
			return Boolean.class;
		return null;
	}

	/**
	 * Convert a string to a certain type, parsing the string if required
	 * 
	 * @param context
	 *            If creating a URL, use this as the URL's context
	 * @param string
	 *            String to convert
	 * @param type
	 *            Type to convert to
	 * @return Converted string, or null if failed
	 */
	protected static Object convertStringToType(URL context, String string, Class<?> type)
	{
		try
		{
			if (type.isAssignableFrom(String.class))
			{
				return string;
			}
			else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class))
			{
				return Double.valueOf(string);
			}
			else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class))
			{
				return Integer.decode(string);
			}
			else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class))
			{
				return Float.valueOf(string);
			}
			else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class))
			{
				return Long.decode(string);
			}
			else if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class))
			{
				return string.charAt(0);
			}
			else if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class))
			{
				return Byte.decode(string);
			}
			else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class))
			{
				return Boolean.valueOf(string);
			}
			else if (type.isAssignableFrom(URL.class))
			{
				try
				{
					return new URL(context, string);
				}
				catch (MalformedURLException e)
				{
				}
			}
			else if (type.isAssignableFrom(File.class))
			{
				return new File(string);
			}
			else if (type.isAssignableFrom(Color.class))
			{
				int[] ints = splitInts(string);
				if (ints.length == 1)
					return new Color(ints[0]);
				else if (ints.length == 3)
					return new Color(ints[0], ints[1], ints[2]);
				else if (ints.length == 4)
					return new Color(ints[0], ints[1], ints[2], ints[3]);
			}
			else if (type.isAssignableFrom(Dimension.class))
			{
				int[] ints = splitInts(string);
				if (ints.length == 1)
					return new Dimension(ints[0], ints[0]);
				else if (ints.length == 2)
					return new Dimension(ints[0], ints[1]);
			}
			else if (type.isAssignableFrom(Point.class))
			{
				int[] ints = splitInts(string);
				if (ints.length == 1)
					return new Point(ints[0], ints[0]);
				else if (ints.length == 2)
					return new Point(ints[0], ints[1]);
			}
			else if (type.isAssignableFrom(Font.class))
			{
				return Font.decode(string);
			}
			else if (type.isAssignableFrom(Material.class))
			{
				Color color = null;
				int[] ints = splitInts(string);
				if (ints.length == 1)
					color = new Color(ints[0]);
				else if (ints.length == 3)
					color = new Color(ints[0], ints[1], ints[2]);
				else if (ints.length == 4)
					color = new Color(ints[0], ints[1], ints[2], ints[3]);

				if (color != null)
					return new Material(color);
			}
			else if (type.isAssignableFrom(Insets.class))
			{
				int[] ints = splitInts(string);
				if (ints.length == 4)
					return new Insets(ints[0], ints[1], ints[2], ints[3]);
			}
		}
		catch (Exception e)
		{

		}
		return null;
	}

	/**
	 * Split a string into an array of integers
	 * 
	 * @param string
	 * @return Array of ints
	 */
	protected static int[] splitInts(String string)
	{
		String[] split = string.trim().split(",");
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
}
