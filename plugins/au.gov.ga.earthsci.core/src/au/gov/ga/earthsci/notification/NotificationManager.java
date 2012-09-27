package au.gov.ga.earthsci.notification;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
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
 * Plugins can provide additional {@link INotificationReceiver} implementations using the {@value #NOTIFICATION_RECEIVER_EXTENSION_POINT_ID} extension point.
 * These can be discovered at runtime and registered with the manager via the {@link #loadReceivers(IExtensionRegistry, IEclipseContext)} method. 
 * <p/>
 * Additionally, notification categories can be registered using the {@value #NOTIFICATION_CATEGORY_PROVIDER_EXTENSION_POINT_ID} extension point.
 * These can be discovered at runtime and registered with the manager via the {@link #registerNotificationCategories(IExtensionRegistry, IEclipseContext)} method. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class NotificationManager
{
	public static final String NOTIFICATION_RECEIVER_EXTENSION_POINT_ID = "au.gov.ga.earthsci.notification.receiver"; //$NON-NLS-1$
	public static final String NOTIFICATION_CATEGORY_PROVIDER_EXTENSION_POINT_ID = "au.gov.ga.earthsci.notification.categoryProvider"; //$NON-NLS-1$
	
	private Set<INotificationReceiver> receivers;
	private ReadWriteLock receiversLock = new ReentrantReadWriteLock();
	
	private ExecutorService notifier = Executors.newSingleThreadExecutor(new ThreadFactory(){
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "NotificationManager notifier thread"); //$NON-NLS-1$
		}
	});
	
	private static NotificationManager INSTANCE;
	
	@Inject 
	private Logger logger;
	
	@PostConstruct
	void finalise()
	{
		if (INSTANCE == null)
		{
			INSTANCE = this;
		}
	}
	
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
		logger.info("Registering notification receivers"); //$NON-NLS-1$
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor(NOTIFICATION_RECEIVER_EXTENSION_POINT_ID);
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
			logger.error(e, "Exception while loading receivers"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Registered additional {@link NotificationCategory}s from {@link INotificationCategoryProvider}s registered against the
	 * {@value #NOTIFICATION_CATEGORY_PROVIDER_EXTENSION_POINT_ID} extension point.
	 * <p/>
	 * This method will inject dependencies on retrieved receivers using the provided 
	 * eclipse context, as appropriate.
	 * 
	 * @param registry The extension registry to search for notification receivers
	 * @param context The context to use for dependency injection etc.
	 */
	@Inject
	public void registerNotificationCategories(IExtensionRegistry registry, IEclipseContext context)
	{
		logger.info("Registering notification categories"); //$NON-NLS-1$
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor(NOTIFICATION_CATEGORY_PROVIDER_EXTENSION_POINT_ID);
		try
		{
			for (IConfigurationElement e : config)
			{
				final Object o = e.createExecutableExtension("class"); //$NON-NLS-1$
				if (o instanceof INotificationCategoryProvider)
				{
					ContextInjectionFactory.inject(o, context);
					((INotificationCategoryProvider)o).registerNotificationCategories();
				}
			}
		}
		catch (CoreException e)
		{
			logger.error(e, "Exception while registering categories"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Send the provided notification to all registered receivers
	 * 
	 * @param notification The notification to send.
	 * 
	 * @see #notify(INotification)
	 */
	public static void sendNotification(INotification notification)
	{
		INSTANCE.notify(notification);
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
	
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}
}
