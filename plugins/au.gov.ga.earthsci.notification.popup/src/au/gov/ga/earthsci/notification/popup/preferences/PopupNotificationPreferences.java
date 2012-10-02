package au.gov.ga.earthsci.notification.popup.preferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationLevel;

/**
 * Default implementation of the {@link IPopupNotificationPreferences} interface that
 * binds to preferences via the Eclipse preference mechanism
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class PopupNotificationPreferences implements IPopupNotificationPreferences
{
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=ENABLE_POPUPS)
	private boolean enabled;

	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=SHOW_INFO_NOTIFICATIONS)
	private boolean infoEnabled;
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=SHOW_WARNING_NOTIFICATIONS)
	private boolean warningEnabled;
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=SHOW_ERROR_NOTIFICATIONS)
	private boolean errorEnabled;

	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=POPUP_DURATION)
	private int displayDuration;
	
	@Inject
	@Preference(nodePath=QUALIFIER_ID, value=CATEGORY_FILTER)
	private String enabledCategories;
	
	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public boolean shouldShow(INotification notification)
	{
		if (!enabled || notification == null)
		{
			return false;
		}
		
		if (!enabledFor(notification.getLevel()))
		{
			return false;
		}
		
		return enabledFor(notification.getCategory());
		
	}
	
	private boolean enabledFor(NotificationLevel level)
	{
		switch (level)
		{
			case INFORMATION:
			{
				return enabled && infoEnabled; 
			}
			case WARNING:
			{
				return enabled && warningEnabled;
			}
			case ERROR:
			{
				return enabled && errorEnabled;
			}
		}
		return false;
	}
	
	private boolean enabledFor(NotificationCategory category)
	{
		return enabledCategories == null || enabledCategories.isEmpty() || enabledCategories.contains(category.getId());
	}

	@Override
	public int getDisplayDuration()
	{
		return displayDuration;
	}

}
