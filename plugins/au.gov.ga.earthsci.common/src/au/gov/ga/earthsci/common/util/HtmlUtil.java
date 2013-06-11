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
 * Utility class for working with HTML.
 */
public class HtmlUtil
{
	/**
	 * Convert text to HTML, replacing any illegal HTML characters ('&amp;',
	 * '&quot;', '&lt;', '&gt;') with their entities.
	 * 
	 * @param content
	 *            Content to convert
	 * @return HTML content
	 */
	public static String convertToHTMLContent(String content)
	{
		content = Util.replace(content, '&', "&amp;"); //$NON-NLS-1$
		content = Util.replace(content, '"', "&quot;"); //$NON-NLS-1$
		content = Util.replace(content, '<', "&lt;"); //$NON-NLS-1$
		return Util.replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	/**
	 * Convert text to HTML, replacing any illegal HTML characters ('&amp;',
	 * '&quot;', '&lt;', '&gt;') with their entities.
	 * <p>
	 * Keeps whitespace, and replaces newlines with the &lt;br/&gt; tag.
	 * 
	 * @param content
	 *            Content to convert
	 * @return HTML content
	 */
	public static String convertToHTMLContentWithWhitespace(String content)
	{
		content = Util.replace(content, '&', "&amp;"); //$NON-NLS-1$
		content = Util.replace(content, '"', "&quot;"); //$NON-NLS-1$
		content = Util.replace(content, '<', "&lt;"); //$NON-NLS-1$
		content = Util.replace(content, '>', "&gt;"); //$NON-NLS-1$
		content = content.replaceAll("\r\n", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
		content = Util.replace(content, '\r', "<br/>"); //$NON-NLS-1$ 
		content = Util.replace(content, '\n', "<br/>"); //$NON-NLS-1$ 
		return "<span style='white-space:pre-wrap'>" + content + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
