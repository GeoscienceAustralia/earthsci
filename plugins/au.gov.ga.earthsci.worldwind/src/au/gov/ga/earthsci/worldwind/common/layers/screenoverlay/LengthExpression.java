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
package au.gov.ga.earthsci.worldwind.common.layers.screenoverlay;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Represents a length expression.
 * <p/>
 * Lengths can be expressed as an absolute value in pixels ("px") or
 * as a percentage of total screen size ("%").
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LengthExpression
{
	private float value;
	private ExpressionType type = ExpressionType.ABSOLUTE;
	
	private static final Pattern VALID_EXPRESSION = Pattern.compile("([\\d]*\\.[\\d]+|[\\d]+)(px|%)?");
	
	/**
	 * Create a new length expression from the provided expression string.
	 * <p/>
	 * Valid expressions are:
	 * <ul>
	 * 	<li>"Npx" - Create an absolute length of N pixels
	 * 	<li>"N%" - Create a relative length of N % of total size
	 * </ul>
	 * If no suffix is provided, the value is interpreted as an absolute pixel value.
	 * 
	 * @see #getLength(float)
	 */
	public LengthExpression(String expression)
	{
		Validate.notBlank(expression, "A length expression is required.");
		
		Matcher matcher = VALID_EXPRESSION.matcher(expression.trim());
		Validate.isTrue(matcher.matches(), "Invalid expression. Expected an expression of the form [Npx | N% | N]");
		
		this.value = Float.parseFloat(matcher.group(1));
		if (matcher.groupCount() == 2)
		{
			type = ExpressionType.forSuffix(matcher.group(2));
		}
	}
	
	/**
	 * @return The length in pixels of this expression evaluated against the provided screen size value
	 */
	public float getLength(float screenSize)
	{
		if (type == ExpressionType.PERCENTAGE)
		{
			return (value / 100) * screenSize;
		}
		return value;
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		return ((LengthExpression)obj).toString().equalsIgnoreCase(toString());
	}
	
	@Override
	public String toString()
	{
		return value + type.getSuffix();
	}
	
	private static enum ExpressionType
	{
		PERCENTAGE("%"),
		ABSOLUTE("px");
		
		private String suffix;
		
		private ExpressionType(String suffix)
		{
			this.suffix = suffix;
		}
		
		public String getSuffix()
		{
			return suffix;
		}
		
		private static Map<String, ExpressionType> suffixToTypeMap = new HashMap<String, ExpressionType>();
		static
		{
			for (ExpressionType t : ExpressionType.values())
			{
				suffixToTypeMap.put(t.getSuffix(), t);
			}
		}
		
		public static ExpressionType forSuffix(String suffix)
		{
			if (Util.isBlank(suffix))
			{
				return ABSOLUTE;
			}
			return suffixToTypeMap.get(suffix.toLowerCase());
		}
	}
}
