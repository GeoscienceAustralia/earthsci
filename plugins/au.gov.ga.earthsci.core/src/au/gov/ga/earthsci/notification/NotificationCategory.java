package au.gov.ga.earthsci.notification;

import static au.gov.ga.earthsci.notification.Messages.NotificationCategory_Download;
import static au.gov.ga.earthsci.notification.Messages.NotificationCategory_General;
import static au.gov.ga.earthsci.notification.Messages.NotificationCategory_IO;

import java.util.HashMap;

/**
 * A class that represents a categorisation of notifications.
 * <p/>
 * Useful for filtering and grouping notifications etc.
 * <p/>
 * Categories are singletons with a unique ID, and additional categories can
 * be registered by plugins.
 * <p/>
 * Categories can be registered using a globally unique ID string, along with a human-readable label for use in preferences etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationCategory
{
	public static final NotificationCategory FILE_IO;
	public static final NotificationCategory DOWNLOAD;
	public static final NotificationCategory GENERAL;
	
	/** The global map of known valid categories */
	private static HashMap<String, NotificationCategory> categories = new HashMap<String, NotificationCategory>();
	static
	{
		FILE_IO = registerCategory("au.gov.ga.earthsci.notification.category.io", NotificationCategory_IO); //$NON-NLS-1$
		DOWNLOAD = registerCategory("au.gov.ga.earthsci.notification.category.download",  NotificationCategory_Download); //$NON-NLS-1$
		GENERAL = registerCategory("au.gov.ga.earthsci.notification.category.general", NotificationCategory_General); //$NON-NLS-1$
	}
	
	/**
	 * @return Whether the provided ID is a valid category ID
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
			throw new IllegalArgumentException("A category with ID " + id + " already exists");  //$NON-NLS-1$//$NON-NLS-2$
		}
		this.id = id;
		this.label = id;
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
		
		return ((NotificationCategory)obj).id.equals(id);
	}
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
