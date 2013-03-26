package au.gov.ga.earthsci.notification;

import java.util.Date;

/**
 * An interface for notifications. This represents the model for the
 * notification mechanism. Notifications are rendered in the user interface
 * using one or more renderers.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface INotification
{
	/**
	 * @return The level of this notification
	 */
	NotificationLevel getLevel();

	/**
	 * @return The category this notification falls in
	 */
	NotificationCategory getCategory();

	/**
	 * @return The ID of this notification (unique within an execution of the
	 *         application)
	 */
	long getId();

	/**
	 * @return The title associated with this notification
	 */
	String getTitle();

	/**
	 * @return The text associated with this notification
	 */
	String getText();

	/**
	 * @return The ordered list of actions associated with this notification
	 */
	INotificationAction[] getActions();

	/**
	 * @return Whether this notification requires an acknowledgement from the
	 *         user
	 */
	boolean requiresAcknowledgment();

	/**
	 * @return The special action associated with the user acknowledging the
	 *         notification (if applicable)
	 */
	INotificationAction getAcknowledgementAction();

	/**
	 * @return Whether this notification has been acknowledged (if applicable)
	 */
	boolean isAcknowledged();

	/**
	 * Acknowledge that the user has received the notification
	 */
	void acknowledge();

	/**
	 * @return The timestamp of when the notification was created
	 */
	Date getCreationTimestamp();

	/**
	 * @return The timestamp of when the notification was acknowledged by the
	 *         user (if applicable)
	 */
	Date getAcknowledgementTimestamp();

	/**
	 * @return Any {@link Throwable} associated with this notification
	 */
	Throwable getThrowable();
}
