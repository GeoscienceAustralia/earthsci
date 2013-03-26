/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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

/**
 * Helper class used to format exceptions to strings and HTML.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExceptionFormatter
{
	public static String toHTML(Throwable t)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>");
		sb.append(t.getClass());
		sb.append(": ");
		sb.append(t.getLocalizedMessage());

		StackTraceElement[] elements = t.getStackTrace();
		for (StackTraceElement element : elements)
		{
			sb.append("<br/>");
			sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
			sb.append(element.toString());
			
		}
		sb.append("</body></html>");
		return sb.toString();
	}
}
