package au.gov.ga.earthsci.notification;


/**
 * A singleton that gives a centralised access to the user notification mechanism.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationManager
{
	private NotificationManager(){}
	
	/**
	 * Generate a notification with the given level and message
	 * 
	 * @param level The level of the notification
	 * @param messageKey 
	 */
	public static void generate(NotificationLevel level, String messageKey)
	{
		
	}
}
