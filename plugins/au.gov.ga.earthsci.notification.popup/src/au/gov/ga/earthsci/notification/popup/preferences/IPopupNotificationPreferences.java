package au.gov.ga.earthsci.notification.popup.preferences;

import au.gov.ga.earthsci.notification.INotification;

/**
 * An interface that gives access to the preferences that control 
 * the popup notification mechanism
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IPopupNotificationPreferences
{
	public static final String QUALIFIER_ID = "au.gov.ga.earthsci.notification.popup"; //$NON-NLS-1$
	public static final String ENABLE_POPUPS = "au.gov.ga.earthsci.notification.popup.preferences.enablePopups"; //$NON-NLS-1$
	public static final String SHOW_INFO_NOTIFICATIONS = "au.gov.ga.earthsci.notification.popup.preferences.showInformationNotifications"; //$NON-NLS-1$
	public static final String SHOW_WARNING_NOTIFICATIONS = "au.gov.ga.earthsci.notification.popup.preferences.showWarningNotifications"; //$NON-NLS-1$
	public static final String SHOW_ERROR_NOTIFICATIONS = "au.gov.ga.earthsci.notification.popup.preferences.showErrorNotifications"; //$NON-NLS-1$
	public static final String POPUP_DURATION = "au.gov.ga.earthsci.notification.popup.preferences.popupDuration"; //$NON-NLS-1$
	public static final String CATEGORY_FILTER = "au.gov.ga.earthsci.notification.popup.preferences.categoryFilter"; //$NON-NLS-1$
	
	
	/**
	 * @return Whether popup notifications are currently enabled
	 */
	boolean isEnabled();
	
	/**
	 * @return Whether the current preferences should allow the provided notification to be shown
	 */
	boolean shouldShow(INotification notification);
	
	/**
	 * @return How long the popup should be displayed for (in ms) 
	 */
	int getDisplayDuration();
}
