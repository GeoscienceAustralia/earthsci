package au.gov.ga.earthsci.notification.popup;

import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationReceiver;
import au.gov.ga.earthsci.notification.NotificationManager;
import au.gov.ga.earthsci.notification.popup.ui.PopupNotification;

/**
 * An {@link INotificationReceiver} that generates a popup for each notification received.
 * <p/>
 * Implementation is based on the hexapixel tutorial available 
 * <a href="http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget">here</a>.
 * 
 * @see http://hexapixel.com/2009/06/30/creating-a-notification-popup-widget
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PopupNotificationReceiver implements INotificationReceiver
{

	@Override
	public void handle(final INotification notification, NotificationManager manager)
	{
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run()
			{
				PopupNotification.show(notification);
			}
		});
	}

}
