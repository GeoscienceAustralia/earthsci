package au.gov.ga.earthsci.core.model.catalog;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory class that uses registered {@link ICatalogProvider}s to 
 * load {@link ICatalogTreeNode} trees from a provided source URI.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class CatalogFactory
{
	@Inject
	public void setup(IExtensionRegistry registry)
	{
		loadProvidersFromRegistry(registry);
	}

	public static final String CATALOG_PROVIDER_EXTENSION_POINT_ID = "au.gov.ga.earthsci.core.model.catalog.provider"; //$NON-NLS-1$
	public static final String CATALOG_PROVIDER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	
	private static final Logger logger = LoggerFactory.getLogger(CatalogFactory.class);
	
	private static final List<ICatalogProvider> registeredProviders = new ArrayList<ICatalogProvider>();
	private static final ReadWriteLock registeredProvidersLock = new ReentrantReadWriteLock();
	
	public static void loadProvidersFromRegistry(IExtensionRegistry registry)
	{
		logger.info("Registering catalog providers"); //$NON-NLS-1$
		
		IConfigurationElement[] config = registry.getConfigurationElementsFor(CATALOG_PROVIDER_EXTENSION_POINT_ID);
		try
		{
			for (IConfigurationElement e : config)
			{
				final Object o = e.createExecutableExtension(CATALOG_PROVIDER_CLASS_ATTRIBUTE); 
				if (o instanceof ICatalogProvider)
				{
					registerProvider((ICatalogProvider)o);
				}
			}
		}
		catch (CoreException e)
		{
			logger.error("Exception while loading providers", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the {@link ICatalogProvider} to use for loading a catalog from the provided source.
	 * <p/>
	 * The short-cut method {@link #loadCatalog(URI)} can be used to load a catalog directly.
	 * 
	 * @param source The source to load the catalog from
	 * 
	 * @return The {@link ICatalogProvider} that can be used to load a catalog from the provided source, or <code>null</code>
	 * if one cannot be found.
	 * 
	 * @see #loadCatalog(URI)
	 */
	public static ICatalogProvider getProvider(URI source)
	{
		if (source == null)
		{
			return null;
		}
		
		registeredProvidersLock.readLock().lock();
		try
		{
			for (ICatalogProvider provider : registeredProviders)
			{
				if (provider.supports(source))
				{
					logger.trace("Found catalog provider {} for source {}", provider, source); //$NON-NLS-1$
					return provider;
				}
			}
			
			logger.trace("No catalog provider found for source {}", source); //$NON-NLS-1$
			return null;
		}
		finally
		{
			registeredProvidersLock.readLock().unlock();
		}
	}
	
	/**
	 * Load and return a catalog from the provided source object.
	 * <p/>
	 * Registered {@link ICatalogProvider}s will be checked in turn. The first that returns <code>true</code> for {@link ICatalogProvider#supports(URI)}
	 * will be used internally to load the catalog.
	 * 
	 * @param source The source to load the catalog from
	 * 
	 * @return The root node of the loaded catalog, or <code>null</code> if unable to load a catalog from the provided source.
	 */
	public static ICatalogTreeNode loadCatalog(URI source)
	{
		logger.trace("Attempting to load catalog from source {}", source); //$NON-NLS-1$
		
		if (source == null)
		{
			return null;
		}
		
		ICatalogProvider provider = getProvider(source);
		if (provider == null)
		{
			return null;
		}
		
		try
		{
			return provider.loadCatalog(source);
		}
		catch (Exception e)
		{
			logger.debug("Unable to load catalog from source " + source + " with provider " + provider.getClass(), e);  //$NON-NLS-1$//$NON-NLS-2$
			return null;
		}
	}
	
	/**
	 * Register the provided {@link ICatalogProvider} on this factory.
	 *  
	 * @param p The catalog provider to add.
	 */
	public static void registerProvider(ICatalogProvider p)
	{
		if (p == null)
		{
			return;
		}
		
		registeredProvidersLock.writeLock().lock();
		try
		{
			registeredProviders.add(p);
		}
		finally
		{
			registeredProvidersLock.writeLock().unlock();
		}
		
		logger.debug("Registered catalog provider: {}", p); //$NON-NLS-1$
	}
	
	/**
	 * Remove the provided {@link ICatalogProvider} from this factory
	 * 
	 * @param p The catalog provider to remove
	 */
	public static void removeProvider(ICatalogProvider p)
	{
		if (p == null)
		{
			return;
		}
		
		registeredProvidersLock.writeLock().lock();
		try
		{
			registeredProviders.remove(p);
		}
		finally
		{
			registeredProvidersLock.writeLock().unlock();
		}
		
		logger.debug("Removed catalog provider: {}", p); //$NON-NLS-1$
	}
	
	/**
	 * Reset this factory and remove all registered providers
	 */
	public static void reset()
	{
		registeredProvidersLock.writeLock().lock();
		try
		{
			registeredProviders.clear();
		}
		finally
		{
			registeredProvidersLock.writeLock().unlock();
		}
		
		logger.debug("Removed catalog all providers"); //$NON-NLS-1$
	}
	
}