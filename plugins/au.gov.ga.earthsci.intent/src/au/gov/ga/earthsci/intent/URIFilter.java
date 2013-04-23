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
package au.gov.ga.earthsci.intent;

import java.net.URI;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Description of URI properties to match.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URIFilter
{
	private String scheme;
	private String authority;
	private String path;

	public URIFilter(IConfigurationElement element)
	{
		scheme = element.getAttribute("scheme"); //$NON-NLS-1$
		authority = element.getAttribute("authority"); //$NON-NLS-1$
		path = element.getAttribute("path"); //$NON-NLS-1$
	}

	/**
	 * @return URI scheme that this filter matches against.
	 */
	public String getScheme()
	{
		return scheme;
	}

	/**
	 * Set the URI scheme that this filter matches against. Can contain the '*'
	 * wildcard character. If "*", matches any scheme.
	 * 
	 * @param scheme
	 * @return this
	 */
	public URIFilter setScheme(String scheme)
	{
		this.scheme = scheme;
		return this;
	}

	/**
	 * @return URI authority that this filter matches against.
	 */
	public String getAuthority()
	{
		return authority;
	}

	/**
	 * Set the URI authority that this filter matches against, after the scheme
	 * part has already been matched. Can contain the '*' wildcard character. If
	 * blank, matches any authority unless the path attribute is set. If "*",
	 * matches any authority.
	 * 
	 * @param authority
	 * @return this
	 */
	public URIFilter setAuthority(String authority)
	{
		this.authority = authority;
		return this;
	}

	/**
	 * @return URI path that this filter matches against.
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Set the URI path that this filter matches against, after the authority
	 * part has already been matched. Can contain the '*' wildcard character. If
	 * blank or "*", matches any path.
	 * 
	 * @param path
	 * @return this
	 */
	public URIFilter setPath(String path)
	{
		this.path = path;
		return this;
	}

	/**
	 * Check if this filter matches the given URI.
	 * 
	 * @param uri
	 *            URI to check
	 * @return True if this filter matches the given URI
	 */
	public boolean matches(URI uri)
	{
		//cannot match a null URI
		if (uri == null)
		{
			return false;
		}
		//cannot match anything if no scheme is defined
		if (isEmpty(scheme))
		{
			return false;
		}
		//no way of matching if authority is blank and path isn't
		if (isEmpty(authority) && !isEmpty(path))
		{
			return false;
		}

		//check if scheme matches
		if (!matchesUsingWildcards(uri.getScheme(), scheme))
		{
			return false;
		}
		if (!isEmpty(authority))
		{
			//check if authority matches
			if (!matchesUsingWildcards(uri.getAuthority(), authority))
			{
				return false;
			}

			if (!isEmpty(path))
			{
				//check if path matches
				if (!matchesUsingWildcards(uri.getPath(), path))
				{
					return false;
				}
			}
		}

		return true;
	}

	protected static boolean matchesUsingWildcards(String input, String pattern)
	{
		input = input == null ? "" : input; //$NON-NLS-1$
		String quoted = Pattern.quote(pattern);
		String regex = quoted.replace("*", "\\E.*\\Q"); //$NON-NLS-1$ //$NON-NLS-2$
		return Pattern.matches(regex, input);
	}

	protected static boolean isEmpty(String s)
	{
		return s == null || s.isEmpty();
	}
}
