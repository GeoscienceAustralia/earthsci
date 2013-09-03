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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.intent.locator.IntentResourceLocatorManager;

/**
 * Description of an intent to be performed. Plugins can define an
 * {@link IntentFilter} that can handle matching intents.
 * <p/>
 * Modelled on Android's Intent system.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Intent
{
	private String action;
	private final Set<String> categories = new HashSet<String>();
	private IContentType contentType;
	private URI uri;
	private Class<?> expectedReturnType;
	private Class<?> requiredReturnType;
	private Class<? extends IIntentHandler> handler;
	private final Map<String, Object> extras = new HashMap<String, Object>();
	private int flags;

	/**
	 * @return Action to be performed.
	 */
	public String getAction()
	{
		return action;
	}

	/**
	 * Set the action that this intent performs.
	 * 
	 * @param action
	 * @return this
	 */
	public Intent setAction(String action)
	{
		this.action = action;
		return this;
	}

	/**
	 * @return Categories associated with this intent.
	 */
	public Set<String> getCategories()
	{
		return categories;
	}

	/**
	 * Add a category to this intent.
	 * 
	 * @param category
	 * @return this
	 */
	public Intent addCategory(String category)
	{
		categories.add(category);
		return this;
	}

	/**
	 * Remove a category from this intent.
	 * 
	 * @param category
	 * @return this
	 */
	public Intent removeCategory(String category)
	{
		categories.remove(category);
		return this;
	}

	/**
	 * @return Explicit content type of the data associated with this intent.
	 */
	public IContentType getContentType()
	{
		return contentType;
	}

	/**
	 * Set the explicit content type of the data associated with this intent.
	 * 
	 * @param type
	 * @return this
	 */
	public Intent setContentType(IContentType contentType)
	{
		this.contentType = contentType;
		return this;
	}

	/**
	 * @return The URI of the data associated with this intent.
	 */
	public URI getURI()
	{
		return uri;
	}

	/**
	 * Set the URI of the data associated with this intent.
	 * <p/>
	 * This value may be updated during the Intent lifecycle (to support URI
	 * re-writing etc.).
	 * 
	 * @param uri
	 * @return this
	 */
	public Intent setURI(URI uri)
	{
		this.uri = uri;
		return this;
	}

	/**
	 * @return A URL pointing to the resource identified by this Intent's URI.
	 *         Uses the {@link IntentResourceLocatorManager} for resolving URLs
	 *         from the URI.
	 * @throws MalformedURLException
	 */
	public URL getURL() throws MalformedURLException
	{
		URL url = IntentResourceLocatorManager.getInstance().locate(this);
		if (url != null)
		{
			return url;
		}
		if (getURI() == null)
		{
			return null;
		}
		return uri.toURL();
	}

	/**
	 * @return The expected type of the object returned by the intent handler.
	 */
	public Class<?> getExpectedReturnType()
	{
		return expectedReturnType;
	}

	/**
	 * Set the expected type of the object returned by the intent handler.
	 * 
	 * @param expectedReturnType
	 * @return this
	 */
	public Intent setExpectedReturnType(Class<?> expectedReturnType)
	{
		this.expectedReturnType = expectedReturnType;
		return this;
	}

	/**
	 * @return The type of the object required to be returned by the intent
	 *         handler.
	 */
	public Class<?> getRequiredReturnType()
	{
		return requiredReturnType;
	}

	/**
	 * Set the required return type of the object returned by the intent
	 * handler.
	 * 
	 * @param requiredReturnType
	 * @return this
	 */
	public Intent setRequiredReturnType(Class<?> requiredReturnType)
	{
		this.requiredReturnType = requiredReturnType;
		return this;
	}

	/**
	 * @return The explicit handler class used to handle this intent.
	 */
	public Class<? extends IIntentHandler> getHandler()
	{
		return handler;
	}

	/**
	 * Set the handler class used to handle this intent explicitly. If this is
	 * set, no other fields are required, as the {@link IntentFilter}s will not
	 * be searched to find an appropriate handler.
	 * 
	 * @param handler
	 * @return this
	 */
	public Intent setHandler(Class<? extends IIntentHandler> handler)
	{
		this.handler = handler;
		return this;
	}

	/**
	 * @return Integer flags set on this intent.
	 */
	public int getFlags()
	{
		return flags;
	}

	/**
	 * Set the flags for this intent.
	 * 
	 * @param flags
	 * @return this
	 */
	public Intent setFlags(int flags)
	{
		this.flags = flags;
		return this;
	}

	/**
	 * Add a flag to this intent. Uses a bitwise OR.
	 * 
	 * @param flag
	 * @return this
	 */
	public Intent addFlag(int flag)
	{
		flags = flags | flag;
		return this;
	}

	/**
	 * Check if the given flag is set on this intent, using a bitwise AND.
	 * 
	 * @param flag
	 * @return True if the given flag is set on this intent.
	 */
	public boolean hasFlag(int flag)
	{
		return (flags & flag) == flag;
	}

	/**
	 * @return The extra data map associated with this intent.
	 */
	public Map<String, Object> getExtras()
	{
		return extras;
	}

	/**
	 * Lookup an extra by the given key on this intent.
	 * 
	 * @param key
	 * @return Extra object associated with the given key on this intent.
	 */
	public Object getExtra(String key)
	{
		return extras.get(key);
	}

	/**
	 * Set an extra keyed by the given key on this intent.
	 * 
	 * @param key
	 * @param value
	 * @return this
	 */
	public Intent putExtra(String key, Object value)
	{
		extras.put(key, value);
		return this;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		if (action != null)
		{
			sb.append(", action: " + action); //$NON-NLS-1$
		}

		if (!categories.isEmpty())
		{
			sb.append(", categories: " + Arrays.toString(categories.toArray())); //$NON-NLS-1$
		}

		if (uri != null)
		{
			sb.append(", URI: " + uri); //$NON-NLS-1$
		}

		if (contentType != null)
		{
			sb.append(", content-type: " + contentType.getId()); //$NON-NLS-1$
		}

		if (expectedReturnType != null)
		{
			sb.append(", expected return-type: " + expectedReturnType.getName()); //$NON-NLS-1$
		}

		if (requiredReturnType != null)
		{
			sb.append(", required return-type: " + requiredReturnType.getName()); //$NON-NLS-1$
		}

		return String.format("%s [%s]", getClass().getSimpleName(), sb.length() > 0 ? sb.substring(2) : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
