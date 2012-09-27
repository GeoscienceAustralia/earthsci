package au.gov.ga.earthsci.notification;

/**
 * An interface for service classes that can register additional {@link NotificationCategory}s
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface INotificationCategoryProvider
{

	/**
	 * Invoked to register additional notification categories.
	 * <p/>
	 * Implementing classes should use {@link NotificationCategory#registerCategory(String, String)} to register
	 * additional categories, and should handle cases of conflicting IDs gracefully.
	 */
	void registerNotificationCategories();
	
}
