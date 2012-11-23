package au.gov.ga.earthsci.notification.part;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.INotificationReceiver;

/**
 * A {@link INotificationReceiver} that provides a tabular view of historic
 * notifications which can be dynamically grouped by level, category etc. 
 * <p/>
 * This class maintains the model of historic notifications, and notifies an associated
 * {@link NotificationPart} when a new notification is received.
 * <p/>
 * This receiver will continue to log notifications in the absence of an associated view,
 * so that when/if the view is created it will have access to the historic log. 
 *  
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationPartReceiver implements INotificationReceiver
{

	private NotificationPart view;
	
	private List<INotification> notifications = new ArrayList<INotification>();
	
	@Override
	public void handle(INotification notification)
	{
		notifications.add(notification);
		if (view != null)
		{
			view.refresh(notification);
		}
	}

	/**
	 * @return The list of all notifications received by this receiver, in
	 * the order in which they were received.
	 */
	public List<INotification> getNotifications()
	{
		return notifications;
	}
	
	/**
	 * Set the notification view on this receiver. This is the view which will
	 * be notified of new notifications.
	 */
	public void setView(NotificationPart notificationView)
	{
		this.view = notificationView;
	}

}
