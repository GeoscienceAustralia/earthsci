/*******************************************************************************
 * Copyright 2013 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.common.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.color.io.IColorMapReader;
import au.gov.ga.earthsci.common.util.ExtensionRegistryUtil;
import au.gov.ga.earthsci.common.util.ExtensionRegistryUtil.Callback;

/**
 * A class that gives static access to commonly used colour maps, and acts as a
 * factory for reading/writing colour maps.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ColorMaps
{

	private static final String READERS_EXTENSION_POINT_ID = "au.gov.ga.earthsci.common.color.colormapreaders"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(ColorMaps.class);

	private static final List<ColorMap> maps = new ArrayList<ColorMap>();
	private static final ReadWriteLock mapsLock = new ReentrantReadWriteLock();

	private static List<IColorMapReader> readers = new ArrayList<IColorMapReader>();
	private static ReadWriteLock readersLock = new ReentrantReadWriteLock();

	static
	{
		maps.add(getRGBRainbowMap());
		maps.add(getRBGRainbowMap());
	}

	private ColorMaps()
	{
	}

	private static ColorMap RBG_RAINBOW_MAP;
	private static ColorMap RGB_RAINBOW_MAP;

	/**
	 * Return a read-only view of the list of registered {@link ColorMaps}
	 * 
	 * @return The list of registered color maps
	 */
	public static List<ColorMap> get()
	{
		return Collections.unmodifiableList(maps);
	}

	/**
	 * Add the given {@link ColorMap} to the list of maps maintained by this
	 * class
	 * 
	 * @param map
	 *            The color map to add
	 */
	public static void add(ColorMap map)
	{
		mapsLock.writeLock().lock();
		try
		{
			maps.add(map);
		}
		finally
		{
			mapsLock.writeLock().unlock();
		}
	}

	/**
	 * Remove the given {@link ColorMap} from the list of maps maintained by
	 * this class
	 * 
	 * @param map
	 *            The color map to add
	 */
	public static void remove(ColorMap map)
	{
		mapsLock.writeLock().lock();
		try
		{
			maps.remove(map);
		}
		finally
		{
			mapsLock.writeLock().unlock();
		}
	}

	/**
	 * Return a standard red-blue-green rainbow colour map
	 */
	public static ColorMap getRBGRainbowMap()
	{
		if (RBG_RAINBOW_MAP == null)
		{
			Map<Double, Color> entries = new HashMap<Double, Color>();
			entries.put(0.0, Color.RED);
			entries.put(0.5, Color.BLUE);
			entries.put(1.0, Color.GREEN);
			RBG_RAINBOW_MAP =
					new ColorMap(Messages.ColorMaps_RBGName, Messages.ColorMaps_RBGDescription,
							entries, null, InterpolationMode.INTERPOLATE_HUE, true);
		}
		return RBG_RAINBOW_MAP;
	}

	/**
	 * Return a standard red-green-blue rainbow colour map
	 */
	public static ColorMap getRGBRainbowMap()
	{
		if (RGB_RAINBOW_MAP == null)
		{
			Map<Double, Color> entries = new HashMap<Double, Color>();
			entries.put(0.0, Color.RED);
			entries.put(0.5, Color.GREEN);
			entries.put(1.0, Color.BLUE);
			RGB_RAINBOW_MAP =
					new ColorMap(Messages.ColorMaps_RGBName, Messages.ColorMaps_RGBDescription,
							entries, null, InterpolationMode.INTERPOLATE_HUE, true);
		}
		return RGB_RAINBOW_MAP;
	}

	@Inject
	public static void loadFromExtensions()
	{
		logger.debug("Registering color map readers"); //$NON-NLS-1$
		try
		{
			ExtensionRegistryUtil.createFromExtension(READERS_EXTENSION_POINT_ID,
					CLASS_ATTRIBUTE, IColorMapReader.class,
					new Callback<IColorMapReader>()
					{
						@Override
						public void run(IColorMapReader reader,
								IConfigurationElement element,
								IEclipseContext context)
						{
							registerColorMapReader(reader);
						}
					});
		}
		catch (CoreException e)
		{
			logger.error("Exception occurred while loading reader from extension", e); //$NON-NLS-1$
		}
	}

	/**
	 * Register the provided {@link IColorMapReader} on this class
	 * 
	 * @param reader
	 *            The reader to register
	 */
	public static void registerColorMapReader(IColorMapReader reader)
	{
		if (reader == null)
		{
			return;
		}

		readersLock.writeLock().lock();
		try
		{
			logger.debug("Registering color map reader: {}", reader.getClass()); //$NON-NLS-1$
			readers.add(reader);
		}
		finally
		{
			readersLock.writeLock().unlock();
		}
	}

	/**
	 * Read a ColorMap from the given source, using registered
	 * {@link IColorMapReader} implementations.
	 * 
	 * @param source
	 *            The source object to read from
	 * 
	 * @return The loaded colour map, or <code>null</code> if a colour map could
	 *         not be read from the provided source.
	 */
	public static ColorMap readFrom(Object source)
	{
		if (source == null)
		{
			return null;
		}

		readersLock.readLock().lock();
		try
		{
			for (IColorMapReader reader : readers)
			{
				if (reader.supports(source))
				{
					return reader.read(source);
				}
			}
			logger.debug("No color map reader found that supports source {}", source); //$NON-NLS-1$
			return null;
		}
		catch (Exception e)
		{
			logger.debug("An exception occurred while reading color map from source " + source, e); //$NON-NLS-1$
			return null;
		}
		finally
		{
			readersLock.readLock().unlock();
		}

	}
}
