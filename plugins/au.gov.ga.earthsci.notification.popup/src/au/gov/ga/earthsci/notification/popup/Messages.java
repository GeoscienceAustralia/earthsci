package au.gov.ga.earthsci.notification.popup;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle accessor for popup notifications
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.notification.popup.messages"; //$NON-NLS-1$
	public static String PopupNotification_CloseTooltip;
	public static String PopupNotificationPreferencePage_NotificationCategoryFilterLabel;
	public static String PopupNotificationPreferencePage_NotificationCategoryColumnLabel;
	public static String PopupNotificationPreferences_Description;
	public static String PopupNotificationPreferences_DurationLabel;
	public static String PopupNotificationPreferences_EnablePopupsLabel;
	public static String PopupNotificationPreferences_ErrorLevelLabel;
	public static String PopupNotificationPreferences_InformationLevelLabel;
	public static String PopupNotificationPreferences_NotificationLevelsLabel;
	public static String PopupNotificationPreferences_WarningLevelLabel;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
