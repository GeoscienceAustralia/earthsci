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
package au.gov.ga.earthsci.core.util.message;

import java.text.FieldPosition;
import java.text.MessageFormat;

/**
 * Base class for the {@link IMessageSource} hierarchy. Delegates message retrieval to 
 * specific implementations of the {@link #getMessageInternal(String)} method.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class MessageSourceBase implements IMessageSource
{

	@Override
	public String getMessage(String key)
	{
		return getMessage(key, null, new Object[0]);
	}

	@Override
	public String getMessage(String key, String defaultMsg)
	{
		return getMessage(key, defaultMsg, new Object[0]);
	}

	@Override
	public String getMessage(String key, Object... params)
	{
		return getMessage(key, null, params);
	}

	@Override
	public String getMessage(String key, String defaultMsg, Object... params)
	{
		MessageFormat message = getMessageInternal(key);
		if (message == null)
		{
			if (defaultMsg == null)
			{
				return null;
			}
			else
			{
				return new MessageFormat(defaultMsg).format(params, new StringBuffer(), new FieldPosition(0)).toString();
			}
		}
		
		// Message formats aren't synchronised, so need this to ensure consistent results
		synchronized (message)
		{
			return message.format(params, new StringBuffer(), new FieldPosition(0)).toString();
		}
	}
	
	/**
	 * @return The message format for the given key, or <code>null</code> if one is not found
	 */
	protected abstract MessageFormat getMessageInternal(String key);

}
