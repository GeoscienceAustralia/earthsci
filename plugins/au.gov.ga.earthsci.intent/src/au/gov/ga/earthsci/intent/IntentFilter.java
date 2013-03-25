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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private int priority = 0;
	private final Set<String> actions = new HashSet<String>();
	private final Set<String> categories = new HashSet<String>();
	private final Set<IContentType> contentTypes = new HashSet<IContentType>();
	private final Set<Class<?>> returnTypes = new HashSet<Class<?>>();
	private final Set<String> dataSchemes = new HashSet<String>();
	private final Set<String> dataAuthorities = new HashSet<String>();
	private final Set<String> dataPaths = new HashSet<String>();
	private Class<? extends IIntentHandler> handler;
	private static final Logger logger = LoggerFactory.getLogger(IntentFilter.class);

	public IntentFilter()
	{
	}

	public IntentFilter(IConfigurationElement element) throws ClassNotFoundException
	{
		addToSetFromElements(element, "action", "name", actions); //$NON-NLS-1$ //$NON-NLS-2$
		addToSetFromElements(element, "category", "name", categories); //$NON-NLS-1$ //$NON-NLS-2$
		addToContentTypeSetFromElements(element, "content-type", "id", contentTypes); //$NON-NLS-1$ //$NON-NLS-2$
		addToClassSetFromElements(element, "return-type", "class", returnTypes); //$NON-NLS-1$ //$NON-NLS-2$
		addToSetFromElements(element, "scheme", "name", dataSchemes); //$NON-NLS-1$ //$NON-NLS-2$
		addToSetFromElements(element, "authority", "name", dataAuthorities); //$NON-NLS-1$ //$NON-NLS-2$
		addToSetFromElements(element, "path", "name", dataPaths); //$NON-NLS-1$ //$NON-NLS-2$

		@SuppressWarnings("unchecked")
		Class<? extends IIntentHandler> handler =
				(Class<? extends IIntentHandler>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
		setHandler(handler);
	}

	private void addToSetFromElements(IConfigurationElement element, String childrenName, String attributeName,
			Set<String> set)
	{
		for (IConfigurationElement child : element.getChildren(childrenName))
		{
			String name = child.getAttribute(attributeName);
			if (!isEmpty(name))
				set.add(name);
		}
	}

	private void addToContentTypeSetFromElements(IConfigurationElement element, String childrenName,
			String attributeName, Set<IContentType> set)
	{
		for (IConfigurationElement child : element.getChildren(childrenName))
		{
			String value = child.getAttribute(attributeName);
			if (!isEmpty(value))
			{
				IContentType contentType = Platform.getContentTypeManager().getContentType(value);
				if (contentType != null)
					set.add(contentType);
			}
		}
	}

	private void addToClassSetFromElements(IConfigurationElement element, String childrenName, String attributeName,
			Set<Class<?>> set)
	{
		for (IConfigurationElement child : element.getChildren(childrenName))
		{
			try
			{
				Class<?> c = ExtensionPointHelper.getClassForProperty(child, attributeName);
				if (c != null)
					set.add(c);
			}
			catch (ClassNotFoundException e)
			{
				logger.error("Error processing intent filter return type", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @return Priority of this filter.
	 */
	public int getPriority()
	{
		return priority;
	}

	/**
	 * Set the priority of this filter. If two filter's match an Intent exactly,
	 * the priority is used to determine which one to use.
	 * 
	 * @param priority
	 * @return this
	 */
	public IntentFilter setPriority(int priority)
	{
		this.priority = priority;
		return this;
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
	 * @return Collection of content types that this filter can match against.
	 */
	public Set<IContentType> getContentTypes()
	{
		return contentTypes;
	}

	/**
	 * Add a content type to this filter.
	 * 
	 * @param dataType
	 * @return this
	 */
	public IntentFilter addContentType(IContentType contentType)
	{
		contentTypes.add(contentType);
		return this;
	}

	/**
	 * Remove a content type from this filter.
	 * 
	 * @param dataType
	 * @return this
	 */
	public IntentFilter removeContentType(IContentType contentType)
	{
		contentTypes.remove(contentType);
		return this;
	}

	/**
	 * @return Collection of URI schemes that this filter matches against.
	 */
	public Set<String> getDataSchemes()
	{
		return dataSchemes;
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
	public Class<? extends IIntentHandler> getHandler()
	{
		return handler;
	}

	/**
	 * Set the Intent handler class associated with this filter.
	 * 
	 * @param handler
	 */
	public void setHandler(Class<? extends IIntentHandler> handler)
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

		//if a content type is defined by one but not the other, no chance of matching
		if ((intent.getContentType() == null) != contentTypes.isEmpty())
			return false;

		//if there are content types defined, check if any match the content type of the intent
		if (!contentTypes.isEmpty())
		{
			if (!anyContentTypesMatch(intent.getContentType()))
				return false;
		}

		//if both intent and filter have a return type defined, check that at least one matches
		if (intent.getExpectedReturnType() != null && !returnTypes.isEmpty())
		{
			if (!anyReturnTypesMatch(intent.getExpectedReturnType()))
				return false;
		}

		//if there are any schemes/authorities/paths defined, check if any match the URI of the intent (in that order)
		if (!dataSchemes.isEmpty())
		{
			URI uri = intent.getURI();
			if (uri == null)
				return false;

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

	public boolean anyContentTypesMatch(IContentType expectedContentType)
	{
		if (expectedContentType != null)
			for (IContentType contentType : contentTypes)
				if (expectedContentType.isKindOf(contentType))
					return true;
		return false;
	}

	public boolean anyReturnTypesMatch(Class<?> expectedReturnType)
	{
		if (expectedReturnType != null)
			for (Class<?> returnType : returnTypes)
				if (expectedReturnType.isAssignableFrom(returnType))
					return true;
		return false;
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
