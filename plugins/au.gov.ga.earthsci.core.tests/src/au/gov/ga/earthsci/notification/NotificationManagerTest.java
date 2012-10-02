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
		
		NotificationManager.get().removeAllRecievers();
	}
	
	@Test
	public void testStaticNotifyNoReceivers()
	{
		NotificationManager.sendNotification(Notification.create(NotificationLevel.ERROR, "A title", "Some text").build());
	}
	
	@Test
	public void testStaticNotifySingleReceiver()
	{
		final INotificationReceiver receiver = context.mock(INotificationReceiver.class);
		final INotification notification = Notification.create(NotificationLevel.ERROR, "A title", "Some text").build();
		
		NotificationManager.get().registerReceiver(receiver);
		
		context.checking(new Expectations() {{{
			oneOf(receiver).handle(with(notification), with(NotificationManager.get()));
		}}});
		
		NotificationManager.sendNotification(notification);
	}
	
	@Test
	public void testStaticNotifyRemovedReceiver()
	{
		final INotificationReceiver receiver = context.mock(INotificationReceiver.class);
		final INotification notification = Notification.create(NotificationLevel.ERROR, "A title", "Some text").build();
		
		NotificationManager.get().registerReceiver(receiver);
		NotificationManager.get().removeReceiver(receiver);
		
		context.checking(new Expectations() {{{
			never(receiver).handle(with(notification), with(NotificationManager.get()));
		}}});
		
		NotificationManager.sendNotification(notification);
	}
}
