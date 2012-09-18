package au.gov.ga.earthsci.notification;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle accessor for the Notification mechanism
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.notification.messages"; //$NON-NLS-1$
	public static String NotificationPreferences_Description;
	public static String NotificationPreferences_Title;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
