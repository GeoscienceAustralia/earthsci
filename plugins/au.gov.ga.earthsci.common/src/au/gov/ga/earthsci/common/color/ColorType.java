package au.gov.ga.earthsci.common.color;

import java.util.EnumMap;

/**
 * An enumeration of supported colour spaces
 */
public enum ColorType
{
	/**
	 * 3 components per colour: Red, Green, Blue
	 */
	RGB(Channel.RED, Channel.GREEN, Channel.BLUE),

	/**
	 * 4 components per colour: Red, Green, Blue, Alpha
	 */
	RGBA(Channel.RED, Channel.GREEN, Channel.BLUE, Channel.ALPHA);

	private EnumMap<Channel, Integer> channels = new EnumMap<Channel, Integer>(Channel.class);

	private ColorType(Channel... channels)
	{
		for (int i = 0; i < channels.length; i++)
		{
			this.channels.put(channels[i], i);
		}
	}

	/**
	 * @return The number of components (or channels) in this colour type
	 */
	public int getNumComponents()
	{
		return channels.size();
	}

	/**
	 * @return Whether this color type includes the provided channel
	 */
	public boolean hasChannel(Channel c)
	{
		return channels.containsKey(c);
	}

	/**
	 * @return The index of the provided channel (0-indexed); or -1 if the
	 *         channel does not exist in this color type.
	 */
	public int getChannelIndex(Channel c)
	{
		if (!hasChannel(c))
		{
			return -1;
		}
		return channels.get(c);
	}

	/**
	 * Represents a channel/component in a {@link ColorType}
	 */
	public static enum Channel
	{
		RED,
		GREEN,
		BLUE,
		ALPHA
	};

}
