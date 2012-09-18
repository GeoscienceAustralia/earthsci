package au.gov.ga.earthsci.notification;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A threadsafe manager that gives access to the user notification mechanism.
 * <p/>
 * The manager maintains a list of {@link INotificationReceiver}s which can register to receive user notifications.
 * The manager invokes each of these handlers when a new user notification is received.  
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationManager
{
	private Set<INotificationReceiver> receivers;
	private ReadWriteLock receiversLock = new ReentrantReadWriteLock();
	
	private ExecutorService notifier = Executors.newSingleThreadExecutor(new ThreadFactory(){
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "NotificationManager notifier thread"); //$NON-NLS-1$
		}
	});
	
	public NotificationManager()
	{
		receivers = new LinkedHashSet<INotificationReceiver>();
	}
	
	/**
	 * Notify the user with the provided notification object
	 * 
	 * @param notification
	 */
	public void notify(final INotification notification)
	{
		if (notification == null)
		{
			return;
		}
		
		notifier.execute(new Runnable(){
			@Override
			public void run()
			{
				receiversLock.readLock().lock();
				try
				{
					for (INotificationReceiver receiver : receivers)
					{
						receiver.handle(notification, NotificationManager.this);
					}
				}
				finally
				{
					receiversLock.readLock().unlock();
				}
			}
		});
	}
	
	/**
	 * Register the provided notification receiver so that it is able to receive user notifications as they are generated.
	 * 
	 * @param receiver The receiver to register
	 */
	public void registerReceiver(INotificationReceiver receiver)
	{
		if (receiver == null)
		{
			return;
		}
		
		receiversLock.writeLock().lock();
		receivers.add(receiver);
		receiversLock.writeLock().unlock();
	}
	
	/**
	 * Remove the provided notification receiver so that it no longer receives user notifications as they are generated.
	 * 
	 * @param receiver The receiver to register
	 */
	public void removeReceiver(INotificationReceiver receiver)
	{
		if (receiver == null)
		{
			return;
		}
		
		receiversLock.writeLock().lock();
		receivers.remove(receiver);
		receiversLock.writeLock().unlock();
	}
}
