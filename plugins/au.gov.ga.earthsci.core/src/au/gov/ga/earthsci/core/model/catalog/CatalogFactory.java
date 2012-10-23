package au.gov.ga.earthsci.core.model.catalog;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;

/**
 * A factory class that uses registered {@link ICatalogProvider}s to 
 * load {@link ICatalogTreeNode} trees from a provided source URI.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogFactory
{

	@Inject
	private static Logger logger;
	
	private static final List<ICatalogProvider> registeredProviders = new ArrayList<ICatalogProvider>();
	private static final ReadWriteLock registeredProvidersLock = new ReentrantReadWriteLock();
	
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
					return provider;
				}
			}
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
			logger.debug(e, "Unable to load catalog from source " + source + " with provider " + provider.getClass());  //$NON-NLS-1$//$NON-NLS-2$
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
	}
	
}