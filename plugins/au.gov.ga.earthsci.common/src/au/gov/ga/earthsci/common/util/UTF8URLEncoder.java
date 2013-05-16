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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Helper class for URL encoding/decoding using the UTF-8 charset.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class UTF8URLEncoder
{
	/**
	 * Translates a string into application/x-www-form-urlencoded format using a
	 * specific encoding scheme. This method uses the UTF-8 encoding scheme to
	 * obtain the bytes for unsafe characters.
	 * 
	 * @param s
	 *            String to be translated
	 * @return the translated String
	 * @see URLEncoder#encode(String, String)
	 */
	public static String encode(String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8"); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e)
		{
			//will never occur, as all Java platforms are required to support UTF-8
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Decodes a application/x-www-form-urlencoded string using the UTF-8
	 * encoding scheme. UTF-8 is used to determine what characters are
	 * represented by any consecutive sequences of the form "%xy".
	 * 
	 * @param s
	 *            the String to decode
	 * @return the newly decoded String
	 * @see URLDecoder#decode(String, String)
	 */
	public static String decode(String s)
	{
		try
		{
			return URLDecoder.decode(s, "UTF-8"); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e)
		{
			//will never occur, as all Java platforms are required to support UTF-8
			throw new IllegalStateException(e);
		}
	}
}
