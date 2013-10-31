package au.gov.ga.earthsci.model.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.ExtensionRegistryUtil;
import au.gov.ga.earthsci.common.util.ExtensionRegistryUtil.Callback;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;

/**
 * A registry used to retrieve {@link IModelGeometryRendererCreator} instances
 * for a given {@link IModelGeometry} instance.
 * <p/>
 * Creators can be registered with the registry programmatically using
 * {@link #registerCreator(IModelGeometryRendererCreator) registerCreator}, or
 * via the {@value #EXTENSION_POINT_ID} extension point.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RendererCreatorRegistry
{

	private static final Logger logger = LoggerFactory.getLogger(RendererCreatorRegistry.class);

	public static final String EXTENSION_POINT_ID = "au.gov.ga.earthsci.model.render.geometryRendererCreators"; //$NON-NLS-1$
	public static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	private static final List<IModelGeometryRendererCreator> creators = new ArrayList<IModelGeometryRendererCreator>();
	private static final ReadWriteLock creatorsLock = new ReentrantReadWriteLock();

	/** A default catch-all creator. Can be overridden by injection */
	private static IModelGeometryRendererCreator globalCreator = new LoggingGeometryRendererCreator();

	/**
	 * Load {@link IModelGeometryRendererCreator}s registered against the
	 * {@value #EXTENSION_POINT_ID} extension point.
	 */
	@Inject
	public static void loadFromExtensions(IEclipseContext context)
	{
		logger.debug("Registering model geometry renderer creators"); //$NON-NLS-1$
		try
		{
			ExtensionRegistryUtil.createFromExtension(EXTENSION_POINT_ID, CLASS_ATTRIBUTE,
					IModelGeometryRendererCreator.class, context, new Callback<IModelGeometryRendererCreator>()
					{
						@Override
						public void run(IModelGeometryRendererCreator creator, IConfigurationElement element,
								IEclipseContext context)
						{
							registerCreator(creator);
						}
					});
		}
		catch (Exception e)
		{
			logger.error("Exception occurred while registering model geometry renderer creators", e); //$NON-NLS-1$
		}
	}

	/**
	 * Register the given creator with the registry.
	 * 
	 * @param creator
	 *            The creator to register
	 */
	public static void registerCreator(IModelGeometryRendererCreator creator)
	{
		if (creator == null)
		{
			return;
		}

		logger.debug("Registering model geometry renderer creator: {} ({})", creator.getName(), //$NON-NLS-1$
				creator.getClass().getSimpleName());

		try
		{
			creatorsLock.writeLock().lock();
			creators.add(creator);
		}
		finally
		{
			creatorsLock.writeLock().unlock();
		}
	}

	/**
	 * Retrieve the list of registered {@link IModelGeometryRendererCreator}s
	 * that support the given {@link IModelGeometry} instance, if any.
	 * <p/>
	 * If no creators are found that support the given geometry, will return the
	 * empty list.
	 * 
	 * @param geometry
	 *            The geometry for which a creator is required
	 * 
	 * @return The list of registered {@link IModelGeometryRendererCreator}s
	 *         that support the given geometry instance, or the empty list if
	 *         none are found.
	 */
	public static List<IModelGeometryRendererCreator> getCreators(IModelGeometry geometry)
	{
		try
		{
			creatorsLock.readLock().lock();

			List<IModelGeometryRendererCreator> result = new ArrayList<IModelGeometryRendererCreator>();
			for (IModelGeometryRendererCreator creator : creators)
			{
				if (creator.supports(geometry))
				{
					result.add(creator);
				}
			}
			return result;
		}
		finally
		{
			creatorsLock.readLock().unlock();
		}
	}

	/**
	 * Return the default {@link IModelGeometryRendererCreator} for the given
	 * geometry instance. If no default has been set, the first creator found
	 * that supports the geometry will be used. If no creators are found that
	 * support the geometry, will return <code>null</code>.
	 * 
	 * @param geometry
	 *            The geometry a creator is required for
	 * 
	 * @return A creator to use for the given geometry, or <code>null</code> if
	 *         none are found.
	 */
	public static IModelGeometryRendererCreator getDefaultCreator(IModelGeometry geometry)
	{
		// TODO Add support for a 'default' creator for the geometry type

		List<IModelGeometryRendererCreator> creators = getCreators(geometry);
		if (creators == null || creators.isEmpty())
		{
			return globalCreator;
		}
		return creators.get(0);
	}

	/**
	 * Override the default global renderer creator.
	 */
	public static void setDefaultGlobalCreator(IModelGeometryRendererCreator defaultCreator)
	{
		RendererCreatorRegistry.globalCreator = defaultCreator;
	}

}
