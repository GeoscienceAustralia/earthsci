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
package au.gov.ga.earthsci.worldwind.common.util.message;

import java.text.MessageFormat;

/**
 * A static accessor to get access to the message source from anywhere in the
 * application
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class MessageSourceAccessor
{
	static
	{
		set(new ResourceBundleMessageSource());
		addBundle("messages.commonMessages");
	}

	private static MessageSource messageSource;

	/**
	 * @return The message source to use
	 */
	public static MessageSource get()
	{
		if (messageSource == null)
		{
			throw new IllegalStateException(
					"Message source not set. Call set() first to set the message source.");
		}

		return messageSource;
	}

	/**
	 * Set the message source to use in the application.
	 * 
	 * @param source
	 *            The message source to set.
	 */
	public static void set(MessageSource source)
	{
		messageSource = source;
	}

	/**
	 * Helper function that calls addBundle() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param name
	 *            The base name of the collection of messages to add
	 */
	public static void addBundle(String baseName)
	{
		messageSource.addBundle(baseName);
	}

	/**
	 * Helper function that calls getMessage() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param key
	 *            The key for the message to return
	 * 
	 * @return The message with the provided key, or <code>null</code> if a
	 *         message with the provided key cannot be found
	 */
	public static String getMessage(String key)
	{
		return get().getMessage(key);
	}
	
	/**
	 * Helper function that calls getMessage() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param key
	 *            The key for the message to return
	 * @param params
	 * 			  Parameters to use in message substitution
	 * 
	 * @return The message with the provided key, or <code>null</code> if a
	 *         message with the provided key cannot be found
	 */
	public static String getMessage(String key, Object... params)
	{
		return get().getMessage(key, params);
	}
	
	/**
	 * Helper function that calls getMessage() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param key
	 *            The key for the message to return
	 * @param defaultMsg
	 *            The default message to return if the key cannot be found
	 * 
	 * @return The message with the provided key, or <code>defaultMsg</code> if
	 *         a message with the provided key cannot be found
	 */
	public static String getMessageOrDefault(String key, String defaultMsg)
	{
		return get().getMessage(key, defaultMsg);
	}
	
	/**
	 * Helper function that calls getMessage() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param key
	 *            The key for the message to return
	 * @param defaultMsg
	 *            The default message to return if the key cannot be found
	 * @param params
	 *            Params to use in substitution
	 * 
	 * @return The message with the provided key, or <code>defaultMsg</code> if
	 *         a message with the provided key cannot be found, with parameter
	 *         substitution applied
	 * 
	 * @see MessageFormat
	 */
	public static String getMessageOrDefault(String key, String defaultMsg, Object... params)
	{
		return get().getMessage(key, defaultMsg, params);
	}
}
