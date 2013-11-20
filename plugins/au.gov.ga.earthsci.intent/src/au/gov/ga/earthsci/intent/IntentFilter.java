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
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.ExtensionPointHelper;

/**
 * Description of {@link Intent} values to be matched. Can match against the
 * Intent's action, categories, content type, expected return type, and/or the
 * URI. Provides the ability to find a suitable handler for an Intent.
 * <p/>
 * The intent filtering is performed as follows:
 * <ul>
 * <li>Action matches if any of the given values match the Intent action, or if
 * no actions are specified by this filter.</li>
 * <li>Categories matches if all of the categories in the Intent match
 * categories in this filter.</li>
 * <li>Content type matches if any of the given values match the Intent content
 * type, or both the Intent and the filter don't specify a content type.</li>
 * <li>Returns result matches if the intent's expected return type is defined,
 * and this filter returns a result.</li>
 * <li>Return type matches if any of the given values match the Intent expected
 * return type if defined, or if the filter defines no return types.</li>
 * <li>Data scheme matches if any of the given values match the Intent URI's
 * scheme.</li>
 * <li>Data authority matches if any of the given values match the Intent URI's
 * authority, and the data scheme has already matched.</li>
 * <li>Data path matches if any of the given values match the Intent URI's path,
 * and the data scheme and authority have already matched.</li>
 * </ul>
 * Data schemes, authorities, and paths can contain wildcards '*'.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentFilter
{
	private int priority = 0;
	private final Set<String> actions = new HashSet<String>();
	private final Set<String> categories = new HashSet<String>();
	private final Set<IContentType> contentTypes = new HashSet<IContentType>();
	private boolean returnsResult = false;
	private final Set<Class<?>> returnTypes = new HashSet<Class<?>>();
	private final Set<URIFilter> uriFilters = new HashSet<URIFilter>();
	private Class<? extends IIntentHandler> handler;
	private static final Logger logger = LoggerFactory.getLogger(IntentFilter.class);
	private String label;
	private String description;
	private URL icon;
	private boolean prompt = true;

	public IntentFilter()
	{
	}

	protected IntentFilter(IConfigurationElement element) throws ClassNotFoundException
	{
		addToSetFromElements(element, "action", "name", actions); //$NON-NLS-1$ //$NON-NLS-2$
		addToSetFromElements(element, "category", "name", categories); //$NON-NLS-1$ //$NON-NLS-2$
		addToContentTypeSetFromElements(element, "content-type", "id", contentTypes); //$NON-NLS-1$ //$NON-NLS-2$
		addToClassSetFromElements(element, "return-type", "class", returnTypes); //$NON-NLS-1$ //$NON-NLS-2$

		for (IConfigurationElement uri : element.getChildren("uri")) //$NON-NLS-1$
		{
			uriFilters.add(new URIFilter(uri));
		}

		@SuppressWarnings("unchecked")
		Class<? extends IIntentHandler> handler =
				(Class<? extends IIntentHandler>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
		setHandler(handler);

		int priority = ExtensionPointHelper.getIntegerForProperty(element, "priority", 0); //$NON-NLS-1$
		setPriority(priority);

		boolean returnsResult = ExtensionPointHelper.getBooleanForProperty(element, "returns-result", false); //$NON-NLS-1$
		setReturnsResult(returnsResult);

		label = element.getAttribute("label"); //$NON-NLS-1$
		description = element.getAttribute("description"); //$NON-NLS-1$
		icon = ExtensionPointHelper.getResourceURLForProperty(element, "icon32"); //$NON-NLS-1$
		prompt = ExtensionPointHelper.getBooleanForProperty(element, "prompt", prompt); //$NON-NLS-1$
	}

	protected static void addToSetFromElements(IConfigurationElement element, String childrenName,
			String attributeName, Set<String> set)
	{
		for (IConfigurationElement child : element.getChildren(childrenName))
		{
			String name = child.getAttribute(attributeName);
			if (!isEmpty(name))
			{
				set.add(name);
			}
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
				if (value.indexOf('*') < 0)
				{
					IContentType contentType = Platform.getContentTypeManager().getContentType(value);
					if (contentType != null)
					{
						set.add(contentType);
					}
				}
				else
				{
					IContentType[] types = Platform.getContentTypeManager().getAllContentTypes();
					String wildcardRegex = URIFilter.wildcardRegex(value);
					for (IContentType type : types)
					{
						if (Pattern.matches(wildcardRegex, type.getId()))
						{
							set.add(type);
						}
					}
				}
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
				{
					set.add(c);
				}
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
	 * @return The {@link URIFilter}s used to filter an Intent's URI.
	 */
	public Set<URIFilter> getURIFilters()
	{
		return uriFilters;
	}

	/**
	 * Add a {@link URIFilter} to this filter.
	 * 
	 * @param uriFilter
	 * @return this
	 */
	public IntentFilter addURIFilter(URIFilter uriFilter)
	{
		uriFilters.add(uriFilter);
		return this;
	}

	/**
	 * Remove a {@link URIFilter} from this filter.
	 * 
	 * @param uriFilter
	 * @return this
	 */
	public IntentFilter removeURIFilter(URIFilter uriFilter)
	{
		uriFilters.remove(uriFilter);
		return this;
	}

	/**
	 * @return Does this filter's handler return a result to the intent
	 *         callback?
	 */
	public boolean isReturnsResult()
	{
		return returnsResult;
	}

	/**
	 * Set whether this filter's handler results a result to the intent
	 * callback.
	 * 
	 * @param returnsResult
	 * @return this
	 */
	public IntentFilter setReturnsResult(boolean returnsResult)
	{
		this.returnsResult = returnsResult;
		return this;
	}

	/**
	 * @return The return types that this filter supports, if known
	 */
	public Set<Class<?>> getReturnTypes()
	{
		return returnTypes;
	}

	/**
	 * Add a return type to this filter.
	 * 
	 * @param returnType
	 * @return this
	 */
	public IntentFilter addReturnType(Class<?> returnType)
	{
		returnTypes.add(returnType);
		return this;
	}

	/**
	 * Remove a return type from this filter.
	 * 
	 * @param returnType
	 * @return this
	 */
	public IntentFilter removeReturnType(Class<?> returnType)
	{
		returnTypes.remove(returnType);
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
	 * @return The label to show to the user if multiple filters match an
	 *         intent.
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Set the label to show to the user if multiple filters match an intent.
	 * 
	 * @param label
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * @return The description to show to the user if multiple filters match an
	 *         intent.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Set the description to show to the user if multiple filters match an
	 * intent.
	 * 
	 * @param description
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return The icon to show to the user if multiple filters match an intent.
	 */
	public URL getIcon()
	{
		return icon;
	}

	/**
	 * Set the icon to show to the user if multiple filters match an intent.
	 * 
	 * @param icon
	 */
	public void setIcon(URL icon)
	{
		this.icon = icon;
	}

	/**
	 * @return Should this filter be shown in the list of filters prompt when
	 *         multiple filters match an Intent?
	 */
	public boolean isPrompt()
	{
		return prompt;
	}

	/**
	 * Set whether this filter be shown in the list of filters prompt when
	 * multiple filters match an Intent.
	 * 
	 * @param prompt
	 */
	public void setPrompt(boolean prompt)
	{
		this.prompt = prompt;
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
		{
			return false;
		}

		//first check intent action
		if (!actions.isEmpty() && !actions.contains(intent.getAction()))
		{
			return false;
		}

		//next check that this contains all intent categories
		if (!categories.containsAll(intent.getCategories()))
		{
			return false;
		}

		//if a content type is defined by one but not the other, no chance of matching
		IContentType contentType = intent.getContentType();
		if ((contentType == null) != contentTypes.isEmpty())
		{
			return false;
		}

		//if there are content types defined, check if any match the content type of the intent
		if (!contentTypes.isEmpty())
		{
			if (!anyContentTypesMatch(contentType))
			{
				return false;
			}
		}

		/*
		//check content type
		IContentType contentType = intent.getContentType();
		if (contentType != null)
		{
			//if a content type is defined, but intent doesn't handle any content types, no chance of matching
			if (contentTypes.isEmpty())
			{
				return false;
			}

			//if there are content types defined, check if any match the content type of the intent
			if (!anyContentTypesMatch(contentType))
			{
				return false;
			}
		}
		*/

		//if the intent has an expected or required return type
		if (intent.getExpectedReturnType() != null || intent.getRequiredReturnType() != null)
		{
			//check that this filter returns a result
			if (!returnsResult)
			{
				return false;
			}

			//if the intent requires a certain type, check that one matches
			if (intent.getRequiredReturnType() != null)
			{
				if (!anyReturnTypesMatch(intent.getRequiredReturnType()))
				{
					return false;
				}
			}
			//if the filter knows which types it returns, check that at least one matches
			else if (!returnTypes.isEmpty()) //expectedReturnType is non-null
			{
				if (!anyReturnTypesMatch(intent.getExpectedReturnType()))
				{
					return false;
				}
			}
		}
		else
		{
			//Sometimes an intent will be raised that doesn't know if it will produce a result or not, so an expected/required
			//return type is not set. These intents should still be able to be matched by filters that return results.
		}

		//if there are any schemes/authorities/paths defined, check if any match the URI of the intent (in that order)
		if (!uriFilters.isEmpty())
		{
			if (intent.getURI() == null)
			{
				return false;
			}
			if (!anyURIFiltersMatch(intent.getURI()))
			{
				return false;
			}
		}

		return true;
	}

	protected boolean anyContentTypesMatch(IContentType expectedContentType)
	{
		if (expectedContentType != null)
		{
			for (IContentType contentType : contentTypes)
			{
				if (expectedContentType.isKindOf(contentType))
				{
					return true;
				}
			}
		}
		return false;
	}

	protected boolean anyReturnTypesMatch(Class<?> expectedReturnType)
	{
		if (expectedReturnType != null)
		{
			for (Class<?> returnType : returnTypes)
			{
				if (expectedReturnType.isAssignableFrom(returnType))
				{
					return true;
				}
			}
		}
		return false;
	}

	protected boolean anyURIFiltersMatch(URI uri)
	{
		for (URIFilter uriFilter : uriFilters)
		{
			if (uriFilter.matches(uri))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isEmpty(String s)
	{
		return s == null || s.isEmpty();
	}
}
