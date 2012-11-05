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
package au.gov.ga.earthsci.worldwind.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility method for working with URLs
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLUtil
{
	public static boolean isHttpsUrl(URL url)
	{
		return url != null && "https".equalsIgnoreCase(url.getProtocol());
	}

	public static boolean isHttpUrl(URL url)
	{
		return url != null && "http".equalsIgnoreCase(url.getProtocol());
	}

	public static boolean isFileUrl(URL url)
	{
		return url != null && "file".equalsIgnoreCase(url.getProtocol());
	}

	public static boolean isForResourceWithExtension(URL url, String extension)
	{
		return url != null && !Util.isBlank(extension) && url.getPath().endsWith(extension);
	}

	/**
	 * @return A new URL with the same protocol and path, but devoid of the
	 *         query string
	 */
	public static URL stripQuery(URL url)
	{
		if (url == null || url.getQuery() == null)
		{
			return url;
		}
		try
		{
			String urlString = url.toExternalForm();
			return new URL(urlString.substring(0, urlString.indexOf('?')));
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	/**
	 * Obtain a {@link File} instance that points to the provided file:// URL.
	 * <p/>
	 * If the provided URL is not a file, will return <code>null</code>.
	 */
	public static File urlToFile(URL url)
	{
		if (!URLUtil.isFileUrl(url))
		{
			return null;
		}

		try
		{
			return new File(url.toURI());
		}
		catch (Exception e1)
		{
			try
			{
				return new File(url.getPath());
			}
			catch (Exception e2)
			{
			}
		}

		return null;
	}

	/**
	 * Create a URL from the provided string. If the url is malformed, will
	 * return <code>null</code>.
	 */
	public static URL fromString(String urlString)
	{
		try
		{
			return new URL(urlString);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Create a URL from the provided string. If the url is malformed, will
	 * prepend the provided protocol string and try again. If it still fails, return <code>null</code>.
	 */
	public static URL fromString(String urlString, String protocol)
	{
		URL result = fromString(urlString);
		if (result != null)
		{
			return result;
		}
		if (!protocol.endsWith("://") || !protocol.endsWith(":/"))
		{
			protocol = protocol + "://";
		}
		return fromString(protocol + urlString);
	}

	/**
	 * Attempts to create a URL from the provided object.
	 * <p/>
	 * Can convert:
	 * <ul>
	 * <li>URL
	 * <li>File
	 * <li>String
	 * </ul>
	 */
	public static URL fromObject(Object source)
	{
		if (source == null)
		{
			return null;
		}
		if (source instanceof URL)
		{
			return (URL) source;
		}
		if (source instanceof File)
		{
			try
			{
				return ((File) source).toURI().toURL();
			}
			catch (MalformedURLException e)
			{
				return null;
			}
		}
		if (source instanceof String)
		{
			return fromString((String) source);
		}
		return null;
	}

	/**
	 * Attempts to create a {@link URI} from the provided {@link URL}.
	 * <p/>
	 * If the input {@link URL} is <code>null</code>, or cannot be converted to
	 * a {@link URI}, will return <code>null</code>.
	 * 
	 * @param url
	 *            The URL to convert
	 * 
	 * @return A {@link URI} representation of the provided {@link URL}, or
	 *         <code>null</code> if one cannot be created
	 */
	public static URI toURI(URL url)
	{
		if (url == null)
		{
			return null;
		}
		try
		{
			return url.toURI();
		}
		catch (URISyntaxException e)
		{
			return null;
		}
	}
}
