package au.gov.ga.earthsci.notification;

/**
 * An interface for classes that can register to receive user notifications.
 * <p/>
 * Implementing classes are responsible for maintaining their own model of
 * notifications etc. and for honouring the acknowledgement flags appropriately.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface INotificationReceiver
{
	/**
	 * Handle the provided notification however is appropriate for this receiver
	 * 
	 * @param notification
	 *            The notification to handle
	 * @param manager
	 *            The notification manager that issued the notification
	 */
	void handle(INotification notification);
}
