package au.gov.ga.earthsci.notification.popup;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationReceiver;
import au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences;
import au.gov.ga.earthsci.notification.popup.preferences.PopupNotificationPreferences;
import au.gov.ga.earthsci.notification.popup.ui.PopupNotification;

/**
 * An {@link INotificationReceiver} that generates a popup for each notification received.
 * <p/>
 * The behaviour of this receiver can be controlled through the {@link PopupNotificationPreferences}.
 * <p/>
 * This receiver will not display any notification which requires acknowledgement as it does not
 * block the UI at all.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
//@Creatable
@Singleton
public class PopupNotificationReceiver implements INotificationReceiver
{

	@Inject
	private IPopupNotificationPreferences preferences;
	
	@Override
	public void handle(final INotification notification)
	{
		if (notification.requiresAcknowledgment() || !preferences.shouldShow(notification))
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
