package au.gov.ga.earthsci.notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import au.gov.ga.earthsci.common.util.message.MessageSourceAccessor;

/**
 * A class that represents a categorisation of notifications.
 * <p/>
 * Useful for filtering and grouping notifications etc.
 * <p/>
 * Categories are singletons with a unique ID, and additional categories can be
 * registered by plugins.
 * <p/>
 * Categories can be registered using a globally unique ID string, along with a
 * human-readable label for use in preferences etc. This label can be looked up
 * from message bundles using the {@link MessageSourceAccessor} mechanism. In
 * this case the category ID will be used to resolve the label. This requires
 * that contributing plugins have registered the appropriate message bundles
 * with {@link MessageSourceAccessor#addBundle(String)}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationCategory implements Serializable
{
	public static final NotificationCategory FILE_IO;
	public static final NotificationCategory DOWNLOAD;
	public static final NotificationCategory GENERAL;

	/** The global map of known valid categories */
	private static HashMap<String, NotificationCategory> categories = new HashMap<String, NotificationCategory>();
	static
	{
		MessageSourceAccessor.addBundle("au.gov.ga.earthsci.notification.messages"); //$NON-NLS-1$

		FILE_IO = registerCategory("au.gov.ga.earthsci.notification.category.io"); //$NON-NLS-1$
		DOWNLOAD = registerCategory("au.gov.ga.earthsci.notification.category.download"); //$NON-NLS-1$
		GENERAL = registerCategory("au.gov.ga.earthsci.notification.category.general"); //$NON-NLS-1$
	}

	/**
	 * @return Whether the provided ID is a valid category ID. The label will be
	 *         looked up through the {@link MessageSourceAccessor} mechanism.
	 */
	public static boolean isValid(String id)
	{
		return categories.containsKey(id);
	}

	/**
	 * @return Retrieve the category with the given ID
	 */
	public static NotificationCategory get(String id)
	{
		return categories.get(id);
	}

	/**
	 * @return Register a category with the given ID and label
	 */
	public static NotificationCategory registerCategory(String id, String label)
	{
		if (categories.containsKey(id))
		{
			return categories.get(id);
		}

		NotificationCategory category = new NotificationCategory(id, label);
		categories.put(id, category);
		return category;
	}

	/**
	 * @return Register a category with the given ID and label
	 */
	public static NotificationCategory registerCategory(String id)
	{
		if (categories.containsKey(id))
		{
			return categories.get(id);
		}

		NotificationCategory category = new NotificationCategory(id);
		categories.put(id, category);
		return category;
	}

	/**
	 * @return The (unordered) collection of registered notification categories
	 */
	public static Collection<NotificationCategory> getRegisteredCategories()
	{
		return categories.values();
	}

	private String id;
	private String label;

	private NotificationCategory(String id, String label)
	{
		this(id);
		this.label = label;
	}

	private NotificationCategory(String id)
	{
		if (categories.containsKey(id))
		{
			throw new IllegalArgumentException("A category with ID " + id + " already exists"); //$NON-NLS-1$//$NON-NLS-2$
		}
		this.id = id;
		this.label = MessageSourceAccessor.getMessage(id);
	}

	/**
	 * @return The unique ID of the category
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return The label to use for this category
	 */
	public String getLabel()
	{
		return label;
	}

	@Override
	public String toString()
	{
		return getId();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}

		if (!(obj instanceof NotificationCategory))
		{
			return false;
		}

		return ((NotificationCategory) obj).id.equals(id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	// Store id and labels during serialisation
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeUTF(id);
		out.writeUTF(label);
	}

	// Re-inflate from stored fields
	private void readObject(ObjectInputStream in) throws IOException
	{
		id = in.readUTF();
		label = in.readUTF();
	}

	// Override to return singleton instances for unique IDs
	private Object readResolve()
	{
		return NotificationCategory.registerCategory(id, label);
	}
}
