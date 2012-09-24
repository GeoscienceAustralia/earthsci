package au.gov.ga.earthsci.notification;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.log.Logger;


/**
 * A threadsafe manager that gives access to the user notification mechanism.
 * <p/>
 * The manager maintains a list of {@link INotificationReceiver}s which can register to receive user notifications.
 * The manager invokes each of these handlers when a new user notification is received.  
 * <p/>
 * Plugins can provide additional {@link INotificationReceiver} implementations using the {@value #EXTENSION_POINT_ID} extension point.
 * These can be discovered at runtime and registered with the manager via the {@link #loadReceivers(IExtensionRegistry, IEclipseContext)} method. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class NotificationManager
{
	public static final String EXTENSION_POINT_ID = "au.gov.ga.earthsci.notification.receiver"; //$NON-NLS-1$
	
	private Set<INotificationReceiver> receivers;
	private ReadWriteLock receiversLock = new ReentrantReadWriteLock();
	
	private ExecutorService notifier = Executors.newSingleThreadExecutor(new ThreadFactory(){
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "NotificationManager notifier thread"); //$NON-NLS-1$
		}
	});
	
	@Inject 
	private Logger logger;
	
	public NotificationManager()
	{
		receivers = new LinkedHashSet<INotificationReceiver>();
	}
	
	/**
	 * Load registered notification receivers from the provided extension registry.
	 * <p/>
	 * This method will inject dependencies on retrieved receivers using the provided 
	 * eclipse context, as appropriate.
	 * 
	 * @param registry The extension registry to search for notification receivers
	 * @param context The context to use for dependency injection etc.
	 */
	@Inject
	public void loadReceivers(IExtensionRegistry registry, IEclipseContext context)
	{
		logger.info("Registering notification recievers"); //$NON-NLS-1$
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor(EXTENSION_POINT_ID);
		try
		{
			for (IConfigurationElement e : config)
			{
				final Object o = e.createExecutableExtension("class"); //$NON-NLS-1$
				if (o instanceof INotificationReceiver)
				{
					ContextInjectionFactory.inject(o, context);
					registerReceiver((INotificationReceiver)o);
				}
			}
		}
		catch (CoreException e)
		{
			logger.error(e, "Exception while loading recievers"); //$NON-NLS-1$
			e.printStackTrace();
		}
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
		logger.debug("Registered notification receiver: {0}", receiver.getClass()); //$NON-NLS-1$

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
		
		logger.debug("Removed notification receiver {0}", receiver.getClass()); //$NON-NLS-1$
		
		receiversLock.writeLock().lock();
		receivers.remove(receiver);
		receiversLock.writeLock().unlock();
	}
}
