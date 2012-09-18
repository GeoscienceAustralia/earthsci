package au.gov.ga.earthsci.notification.popup;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationReceiver;
import au.gov.ga.earthsci.notification.NotificationManager;
import au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences;
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
@Creatable
@Singleton
public class PopupNotificationReceiver implements INotificationReceiver
{

	public static void register(IEclipseContext context)
	{
		PopupNotificationReceiver instance = ContextInjectionFactory.make(PopupNotificationReceiver.class, context);
		context.get(NotificationManager.class).registerReceiver(instance);
		
		ContextInjectionFactory.make(PopupNotification.class, context);
	}
	
	@Inject
	private IPopupNotificationPreferences preferences;
	
	@Override
	public void handle(final INotification notification, NotificationManager manager)
	{
		if (!preferences.isEnabledFor(notification.getLevel()))
		{
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run()
			{
				PopupNotification.show(notification);
			}
		});
	}

}
