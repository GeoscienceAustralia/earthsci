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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.eclipse.core.runtime.IConfigurationElement;

import au.gov.ga.earthsci.injectable.ExtensionPointHelper;

/**
 * Description of {@link Intent} values to be matched. Can match against the
 * Intent's action, categories, MIME types, and/or the URI. Provides the ability
 * to find a suitable handler for an Intent.
 * <p/>
 * The intent filtering is performed as follows:
 * <ul>
 * <li>Action matches if any of the given values match the Intent action, or if
 * no actions are specified.</li>
 * <li>Categories matches if all of the categories in the Intent match
 * categories in this filter.</li>
 * <li>Data type matches if any of the given values match the Intent type.</li>
 * <li>Data scheme matches if any of the given values match the Intent URI's
 * scheme.</li>
 * <li>Data authority matches if any of the given values match the Intent URI's
 * authority, and the data scheme has already matched.</li>
 * <li>Data path matches if any of the given values match the Intent URI's path,
 * and the data scheme and authority have already matched.</li>
 * </ul>
 * Data types can contain a wildcard '*' in their MIME subtype. Data schemes,
 * authorities, and paths can also contain wildcards '*'.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentFilter
{
	private final Set<String> actions = new HashSet<String>();
	private final Set<String> categories = new HashSet<String>();
	private final Set<String> dataTypes = new HashSet<String>();
	private final Set<String> dataSchemes = new HashSet<String>();
	private final Set<String> dataAuthorities = new HashSet<String>();
	private final Set<String> dataPaths = new HashSet<String>();
	private Class<? extends IntentHandler> handler;

	public IntentFilter()
	{
	}

	public IntentFilter(IConfigurationElement element) throws ClassNotFoundException
	{
		addToSetFromElements(element, "action", actions); //$NON-NLS-1$
		addToSetFromElements(element, "category", categories); //$NON-NLS-1$
		addToSetFromElements(element, "type", dataTypes); //$NON-NLS-1$
		addToSetFromElements(element, "scheme", dataSchemes); //$NON-NLS-1$
		addToSetFromElements(element, "authority", dataAuthorities); //$NON-NLS-1$
		addToSetFromElements(element, "path", dataPaths); //$NON-NLS-1$

		@SuppressWarnings("unchecked")
		Class<? extends IntentHandler> handler =
				(Class<? extends IntentHandler>) ExtensionPointHelper.getClassForProperty(element, "handler"); //$NON-NLS-1$
		setHandler(handler);
	}

	private void addToSetFromElements(IConfigurationElement element, String childrenName, Set<String> set)
	{
		for (IConfigurationElement child : element.getChildren(childrenName))
		{
			String name = child.getAttribute("name"); //$NON-NLS-1$
			if (!isEmpty(name))
				set.add(name);
		}
	}

	/**
	 * @return Collection of actions, any of which may match an Intent's action.
	 */
	public Set<String> getActions()
	{
		return actions;
	}

	/**
	 * Add an action to this filter.
	 * 
	 * @param action
	 * @return this
	 */
	public IntentFilter addAction(String action)
	{
		actions.add(action);
		return this;
	}

	/**
	 * Remove an action from this filter.
	 * 
	 * @param action
	 * @return this
	 */
	public IntentFilter removeAction(String action)
	{
		actions.remove(action);
		return this;
	}

	/**
	 * @return Collection of categories associated with this filter. All
	 *         categories in an Intent must exist in this filter in order for it
	 *         to be matched.
	 */
	public Set<String> getCategories()
	{
		return categories;
	}

	/**
	 * Add a category to this filter.
	 * 
	 * @param category
	 * @return this
	 */
	public IntentFilter addCategory(String category)
	{
		categories.add(category);
		return this;
	}

	/**
	 * Remove a category from this filter.
	 * 
	 * @param category
	 * @return this
	 */
	public IntentFilter removeCategory(String category)
	{
		categories.remove(category);
		return this;
	}

	/**
	 * @return Collection of MIME types that this filter can match against.
	 */
	public Set<String> getDataTypes()
	{
		return dataTypes;
	}

	/**
	 * Add a MIME type to this filter.
	 * 
	 * @param dataType
	 * @return this
	 */
	public IntentFilter addDataType(String dataType)
	{
		dataTypes.add(dataType);
		return this;
	}

	/**
	 * Remove a MIME type from this filter.
	 * 
	 * @param dataType
	 * @return this
	 */
	public IntentFilter removeDataType(String dataType)
	{
		dataTypes.remove(dataType);
		return this;
	}

	/**
	 * @return Collection of URI schemes that this filter matches against.
	 */
	public Set<String> getDataSchemes()
	{
		return dataTypes;
	}

	/**
	 * Add a URI scheme to this filter.
	 * 
	 * @param dataScheme
	 * @return this
	 */
	public IntentFilter addDataScheme(String dataScheme)
	{
		dataSchemes.add(dataScheme);
		return this;
	}

	/**
	 * Remove a URI scheme from this filter.
	 * 
	 * @param dataScheme
	 * @return this
	 */
	public IntentFilter removeDataScheme(String dataScheme)
	{
		dataSchemes.remove(dataScheme);
		return this;
	}

	/**
	 * @return Collection of URI authorities that this filter matches against.
	 */
	public Set<String> getDataAuthorities()
	{
		return dataAuthorities;
	}

	/**
	 * Add a URI authority to this filter.
	 * 
	 * @param dataAuthority
	 * @return this
	 */
	public IntentFilter addDataAuthority(String dataAuthority)
	{
		dataAuthorities.add(dataAuthority);
		return this;
	}

	/**
	 * Remove a URI authority from this filter.
	 * 
	 * @param dataAuthority
	 * @return this
	 */
	public IntentFilter removeDataAuthority(String dataAuthority)
	{
		dataAuthorities.remove(dataAuthority);
		return this;
	}

	/**
	 * @return Collection of URI paths that this filter matches against.
	 */
	public Set<String> getDataPaths()
	{
		return dataPaths;
	}

	/**
	 * Add a URI path to this filter.
	 * 
	 * @param dataPath
	 * @return this
	 */
	public IntentFilter addDataPath(String dataPath)
	{
		dataPaths.add(dataPath);
		return this;
	}

	/**
	 * Remove a URI path from this filter.
	 * 
	 * @param dataPath
	 * @return this
	 */
	public IntentFilter removeDataPath(String dataPath)
	{
		dataPaths.remove(dataPath);
		return this;
	}

	/**
	 * @return Class associated with this filter, used to perform the actual
	 *         Intent handling.
	 */
	public Class<? extends IntentHandler> getHandler()
	{
		return handler;
	}

	/**
	 * Set the Intent handler class associated with this filter.
	 * 
	 * @param handler
	 */
	public void setHandler(Class<? extends IntentHandler> handler)
	{
		this.handler = handler;
	}

	/**
	 * Check if this filter matches the given Intent.
	 * 
	 * @param intent
	 *            Intent to check
	 * @return True if this filter matches.
	 */
	public boolean matches(Intent intent)
	{
		if (intent == null)
			return false;

		//first check intent action
		if (!actions.isEmpty() && !actions.contains(intent.getAction()))
			return false;

		//next check that this contains all intent categories
		if (!categories.containsAll(intent.getCategories()))
			return false;

		//if a MIME type is defined by one but not the other, no chance of matching
		if (isEmpty(intent.getType()) != dataTypes.isEmpty())
			return false;

		//if a URI is defined by one but not the other, no chance of matching
		if ((intent.getURI() == null) != dataSchemes.isEmpty())
			return false;

		//if there are data types defined, check if any match the MIME type of the intent
		if (!dataTypes.isEmpty())
		{
			boolean matchFound = false;
			try
			{
				MimeType mimeType = new MimeType(intent.getType());
				for (String dataType : dataTypes)
				{
					matchFound = mimeType.match(dataType);
					if (matchFound)
						break;
				}
			}
			catch (MimeTypeParseException e)
			{
			}
			if (!matchFound)
				return false;
		}

		//if there are any schemes/authorities/paths defined, check if any match the URI of the intent (in that order)
		URI uri = intent.getURI();
		if (!dataSchemes.isEmpty())
		{
			if (!anyMatchesUsingWildcards(uri.getScheme(), dataSchemes))
				return false;

			if (!dataAuthorities.isEmpty())
			{
				if (!anyMatchesUsingWildcards(uri.getAuthority(), dataAuthorities))
					return false;

				if (!dataPaths.isEmpty())
				{
					if (!anyMatchesUsingWildcards(uri.getPath(), dataPaths))
						return false;
				}
			}
		}

		return true;
	}

	private static boolean isEmpty(String s)
	{
		return s == null || s.isEmpty();
	}

	private static boolean anyMatchesUsingWildcards(String input, Collection<String> patterns)
	{
		for (String pattern : patterns)
		{
			String quoted = Pattern.quote(pattern);
			String regex = quoted.replace("*", "\\E.*\\Q"); //$NON-NLS-1$ //$NON-NLS-2$
			if (Pattern.matches(regex, input))
				return true;
		}
		return false;
	}
}
