package au.gov.ga.earthsci.notification.popup;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationReceiver;
import au.gov.ga.earthsci.notification.NotificationManager;
import au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences;
import au.gov.ga.earthsci.notification.popup.ui.PopupNotification;

/**
 * An {@link INotificationReceiver} that generates a popup for each notification received.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class PopupNotificationReceiver implements INotificationReceiver
{

	@Inject
	private IPopupNotificationPreferences preferences;
	
	@Override
	public void handle(final INotification notification, NotificationManager manager)
	{
		if (!preferences.shouldShow(notification))
		{
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run()
			{
				PopupNotification.show(notification, preferences);
			}
		});
	}

	public void setPreferences(IPopupNotificationPreferences preferences)
	{
		this.preferences = preferences;
	}
	
}
