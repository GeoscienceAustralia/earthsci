package au.gov.ga.earthsci.worldwind.common.util.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * An extension of {@link ResourceBundleMessageSource} that uses a backing map
 * to override source messages if required.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class StaticMessageSource extends ResourceBundleMessageSource
{
	/** The backing map */
	private Map<String, MessageFormat> messages = new HashMap<String, MessageFormat>();

	/**
	 * Add the provided message to this source
	 * 
	 * @param key
	 *            The key for the message
	 * @param messageFormat
	 *            The message format, formatted according to
	 *            {@link MessageFormat} conventions.
	 */
	public void addMessage(String key, String messageFormat)
	{
		this.messages.put(key, new MessageFormat(messageFormat));
	}

	@Override
	protected MessageFormat getMessageInternal(String key)
	{
		if (messages.containsKey(key))
		{
			return messages.get(key);
		}
		return super.getMessageInternal(key);
	}
}
