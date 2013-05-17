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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A builder class for creating URI instances.
 * <p/>
 * Provides support for adding and escaping parameters etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class URIBuilder
{
	private String scheme;
	private String userInfo;
	private String host;
	private Integer port;
	private String path;
	private Map<String, String> params = new LinkedHashMap<String, String>(); // Gives predictable parameter order
	private String fragment;

	/**
	 * Create a new empty URI builder
	 */
	public URIBuilder()
	{
	};

	/**
	 * Create a new URI builder using values from the given URI as a base
	 */
	public URIBuilder(URI base)
	{
		if (base == null)
		{
			return;
		}

		scheme = base.getScheme();
		userInfo = base.getUserInfo();
		host = base.getHost();
		port = base.getPort() == -1 ? null : base.getPort();
		path = base.getPath();
		fragment = base.getFragment();

		if (Util.isEmpty(base.getQuery()))
		{
			return;
		}

		params.putAll(URIUtil.getParameterMap(base));
	}

	/**
	 * Set the URI scheme, overriding any existing value.
	 */
	public URIBuilder setScheme(String scheme)
	{
		this.scheme = scheme;
		return this;
	}

	/**
	 * Set the fragment on the URI
	 */
	public URIBuilder setFragment(String fragment)
	{
		this.fragment = fragment;
		return this;
	}

	/**
	 * Set the host on the URI
	 */
	public URIBuilder setHost(String host)
	{
		this.host = host;
		return this;
	}

	/**
	 * Set the path on the URI
	 */
	public URIBuilder setPath(String path)
	{
		this.path = path;
		return this;
	}

	/**
	 * Set the port on this URI
	 */
	public URIBuilder setPort(int port)
	{
		this.port = port;
		return this;
	}

	/**
	 * Set the user info on this URI
	 */
	public URIBuilder setUserInfo(String userInfo)
	{
		this.userInfo = userInfo;
		return this;
	}

	/**
	 * Set a query param on this URI, overriding any existing value.
	 * <p/>
	 * The provided value will be encoded as UTF8
	 */
	public URIBuilder setParam(String key, String value)
	{
		if (value == null)
		{
			return setEncodedParam(key, null);
		}
		return setEncodedParam(key, UTF8URLEncoder.encode(value));
	}

	/**
	 * Set an already encoded query param on this URI, overriding any existing
	 * value.
	 */
	public URIBuilder setEncodedParam(String key, String encodedValue)
	{
		this.params.put(key, encodedValue);
		return this;
	}

	private boolean hasAuthority()
	{
		return !Util.isEmpty(userInfo) || !Util.isEmpty(host);
	}

	@SuppressWarnings("nls")
	/**
	 * Build and return the URI from the information collected.
	 * 
	 * @return The URI built from the information collected.
	 * 
	 * @throws URISyntaxException If the resulting URI is invalid
	 */
	public URI build() throws URISyntaxException
	{
		StringBuilder result = new StringBuilder();

		result.append(scheme);
		result.append(':');
		if (hasAuthority())
		{
			result.append("//");
		}
		if (!Util.isEmpty(userInfo))
		{
			result.append(userInfo);
			if (!Util.isEmpty(host))
			{
				result.append('@');
			}
		}
		if (!Util.isEmpty(host))
		{
			result.append(host);
		}
		if (port != null)
		{
			result.append(':').append(port);
		}
		if (!Util.isEmpty(path))
		{
			result.append(path);
		}
		if (!params.isEmpty())
		{
			result.append('?');
			int count = 0;
			for (Entry<String, String> param : params.entrySet())
			{
				if (count > 0)
				{
					result.append('&');
				}
				result.append(param.getKey());
				if (param.getValue() != null)
				{
					result.append('=').append(param.getValue());
				}
				count++;
			}
		}
		if (!Util.isEmpty(fragment))
		{
			result.append("#").append(fragment);
		}
		return new URI(result.toString());
	}
}
