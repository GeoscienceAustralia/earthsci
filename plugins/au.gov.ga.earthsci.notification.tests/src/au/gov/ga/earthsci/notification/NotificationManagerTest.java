package au.gov.ga.earthsci.notification;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link NotificationManager} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationManagerTest
{
	private Mockery context;

	@Before
	public void setup()
	{
		context = new Mockery();

		NotificationManager.removeAllRecievers();
	}

	@Test
	public void testStaticNotifyNoReceivers()
	{
		NotificationManager.notify(Notification.create(NotificationLevel.ERROR, "A title", "Some text").build());
	}

	@Test
	public void testStaticNotifySingleReceiver()
	{
		final INotificationReceiver receiver = context.mock(INotificationReceiver.class);
		final INotification notification = Notification.create(NotificationLevel.ERROR, "A title", "Some text").build();

		NotificationManager.registerReceiver(receiver);

		context.checking(new Expectations()
		{
			{
				{
					oneOf(receiver).handle(with(notification));
				}
			}
		});

		NotificationManager.notify(notification);
	}

	@Test
	public void testStaticNotifyRemovedReceiver()
	{
		final INotificationReceiver receiver = context.mock(INotificationReceiver.class);
		final INotification notification = Notification.create(NotificationLevel.ERROR, "A title", "Some text").build();

		NotificationManager.registerReceiver(receiver);
		NotificationManager.removeReceiver(receiver);

		context.checking(new Expectations()
		{
			{
				{
					never(receiver).handle(with(notification));
				}
			}
		});

		NotificationManager.notify(notification);
	}
}
