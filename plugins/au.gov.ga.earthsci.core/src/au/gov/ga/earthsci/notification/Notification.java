package au.gov.ga.earthsci.notification;

import java.util.Date;
import java.util.UUID;

/**
 * The default implementation of the {@link INotification} interface
 * <p/>
 * Provides a threadsafe implementation that is essentially immutable. Only the
 * acknowledgement indicators may change after object creation, and they can only be changed 
 * through a call {@link #acknowledge()}, which is an idempotent operation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Notification implements INotification
{
	
	private long id;
	private NotificationLevel level;
	
	private long creationTimestamp;
	
	private String title;
	private String text;
	
	private INotificationAction[] actions;
	
	private boolean requiresAcknowledgement = false;
	private boolean acknowledged = false;
	private INotificationAction acknowledgementAction;
	private long acknowledgementTimestamp = -1;
	
	/**
	 * Create a basic notification that has no associated actions and does not require user acknowledgement
	 * 
	 * @param level The level of the notification
	 * @param title The title for the notification
	 * @param text The text to include in the notification
	 */
	public Notification(NotificationLevel level, String title, String text) 
	{
		this(level, title, text, null, false, null);
	}
	
	/**
	 * Create a notification that has associated actions, but does not require user acknowledgement
	 * 
	 * @param level The level of the notification
	 * @param title The title for the notification
	 * @param text The text to include in the notification
	 * @param actions The list of actions to include in the notification
	 */
	public Notification(NotificationLevel level, String title, String text, INotificationAction... actions)
	{
		this(level, title, text, actions, false, null);
	}
	
	/**
	 * Create a fully configured notification
	 * 
	 * @param level The level of the notification
	 * @param title The title for the notification
	 * @param text The text to include in the notification
	 * @param actions The list of actions to include in the notification
	 * @param requiresAcknowledgement Whether or not this notification requires acknowledgement from the user
	 * @param acknowledgementAction The action to associate with user acknowledgement
	 */
	public Notification(NotificationLevel level, String title, String text, INotificationAction[] actions, boolean requiresAckowledgement, INotificationAction acknowledgementAction)
	{
		this.level = level;
		this.title = title;
		this.text = text;
		this.actions = actions;
		this.requiresAcknowledgement = requiresAckowledgement;
		this.acknowledgementAction = acknowledgementAction;
		
		this.creationTimestamp = new Date().getTime();
		
		this.id = UUID.randomUUID().getLeastSignificantBits();
	}
	
	@Override
	public NotificationLevel getLevel()
	{
		return level;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public INotificationAction[] getActions()
	{
		return actions;
	}

	@Override
	public boolean requiresAcknowledgment()
	{
		return requiresAcknowledgement;
	}

	@Override
	public INotificationAction getAcknowledgementAction()
	{
		return acknowledgementAction;
	}

	@Override
	public synchronized boolean isAcknowledged()
	{
		return acknowledged;
	}
	
	@Override
	public Date getCreationTimestamp()
	{
		return new Date(creationTimestamp);
	}

	@Override
	public synchronized Date getAcknowledgementTimestamp()
	{
		if (acknowledgementTimestamp < 0)
		{
			return null;
		}
		return new Date(acknowledgementTimestamp);
	}
	
	@Override
	public synchronized void acknowledge()
	{
		if (isAcknowledged())
		{
			return;
		}
		
		acknowledged = true;
		acknowledgementTimestamp = new Date().getTime();
		
		if (acknowledgementAction != null)
		{
			acknowledgementAction.run();
		}
	}
}
