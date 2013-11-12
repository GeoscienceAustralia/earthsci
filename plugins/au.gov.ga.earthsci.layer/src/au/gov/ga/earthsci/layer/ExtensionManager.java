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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.layers.Layer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.ExtensionPointHelper;
import au.gov.ga.earthsci.layer.wrappers.ILayerWrapper;

/**
 * Manages the list of layers/wrappers/replacements defined using the
 * {@value #LAYERS_ID} extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtensionManager
{
	private static final String LAYERS_ID = "au.gov.ga.earthsci.layers"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(ExtensionManager.class);
	private static ExtensionManager instance = new ExtensionManager();

	/**
	 * @return An instance of the layer extension manager
	 */
	public static ExtensionManager getInstance()
	{
		return instance;
	}

	private final Map<String, Class<? extends IPersistentLayer>> idToLayer =
			new HashMap<String, Class<? extends IPersistentLayer>>();
	private final Map<Class<? extends IPersistentLayer>, String> layerToId =
			new HashMap<Class<? extends IPersistentLayer>, String>();
	private final Map<String, Class<? extends ILayerWrapper>> idToWrapper =
			new HashMap<String, Class<? extends ILayerWrapper>>();
	private final Map<Class<? extends ILayerWrapper>, String> wrapperToId =
			new HashMap<Class<? extends ILayerWrapper>, String>();
	private final Map<Class<? extends Layer>, Class<? extends ILayerWrapper>> layerToWrapper =
			new HashMap<Class<? extends Layer>, Class<? extends ILayerWrapper>>();
	private final Map<Class<? extends ILayerWrapper>, Class<? extends Layer>> wrapperToLayer =
			new HashMap<Class<? extends ILayerWrapper>, Class<? extends Layer>>();
	private final Map<String, Class<? extends IPersistentLayer>> replacementClasses =
			new HashMap<String, Class<? extends IPersistentLayer>>();
	private final Map<String, String> replacementIds = new HashMap<String, String>();

	private ExtensionManager()
	{
		IConfigurationElement[] elements = RegistryFactory.getRegistry().getConfigurationElementsFor(LAYERS_ID);
		for (IConfigurationElement element : elements)
		{
			try
			{
				if ("layer".equals(element.getName())) //$NON-NLS-1$
				{
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (idToLayer.containsKey(id) || idToWrapper.containsKey(id))
					{
						throw new IllegalArgumentException("Layer id '" + id + "' already exists"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					@SuppressWarnings("unchecked")
					Class<? extends IPersistentLayer> layer =
							(Class<? extends IPersistentLayer>) ExtensionPointHelper.getClassForProperty(element,
									"class"); //$NON-NLS-1$
					idToLayer.put(id, layer);
					layerToId.put(layer, id);
				}
				else if ("wrapper".equals(element.getName())) //$NON-NLS-1$
				{
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (idToLayer.containsKey(id) || idToWrapper.containsKey(id))
					{
						throw new IllegalArgumentException("Layer id '" + id + "' already exists"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					@SuppressWarnings("unchecked")
					Class<? extends ILayerWrapper> wrapper =
							(Class<? extends ILayerWrapper>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
					@SuppressWarnings("unchecked")
					Class<? extends Layer> layer =
							(Class<? extends Layer>) ExtensionPointHelper.getClassForProperty(element, "wraps"); //$NON-NLS-1$
					idToWrapper.put(id, wrapper);
					wrapperToId.put(wrapper, id);
					layerToWrapper.put(layer, wrapper);
					wrapperToLayer.put(wrapper, layer);
				}
				else if ("replacementClass".equals(element.getName())) //$NON-NLS-1$
				{
					String replacementFor = element.getAttribute("for"); //$NON-NLS-1$
					if (replacementClasses.containsKey(replacementFor))
					{
						throw new IllegalArgumentException(
								"Layer replacement class already exists for: " + replacementFor); //$NON-NLS-1$
					}
					@SuppressWarnings("unchecked")
					Class<? extends IPersistentLayer> layer =
							(Class<? extends IPersistentLayer>) ExtensionPointHelper.getClassForProperty(element,
									"class"); //$NON-NLS-1$
					replacementClasses.put(replacementFor, layer);
				}
				else if ("replacementId".equals(element.getName())) //$NON-NLS-1$
				{
					String replacementFor = element.getAttribute("for"); //$NON-NLS-1$
					if (replacementIds.containsKey(replacementFor))
					{
						throw new IllegalArgumentException("Layer replacement id already exists for: " + replacementFor); //$NON-NLS-1$
					}
					String replacementId = element.getAttribute("id"); //$NON-NLS-1$
					replacementIds.put(replacementFor, replacementId);
				}
			}
			catch (Exception e)
			{
				logger.error("Error processing layer", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Get the id to use in the XML when persisting the given layer or wrapper.
	 * 
	 * @param layerOrWrapper
	 * @return Id for the given layer or wrapper
	 */
	public String getIdForLayerOrWrapper(IPersistentLayer layerOrWrapper)
	{
		String id = layerToId.get(layerOrWrapper.getClass());
		if (id == null)
		{
			id = wrapperToId.get(layerOrWrapper.getClass());
		}
		return id;
	}

	/**
	 * Get the layer or wrapper class for the given id. Called when unpersisting
	 * a layer/wrapper from XML.
	 * 
	 * @param id
	 * @return Layer or wrapper class associated with the given id
	 */
	public Class<? extends IPersistentLayer> getLayerOrWrapperForId(String id)
	{
		Class<? extends IPersistentLayer> layer = idToLayer.get(id);
		if (layer == null)
		{
			layer = idToWrapper.get(id);
		}
		return layer;
	}

	/**
	 * Get the layer or wrapper class to use as a replacement for the given
	 * layer/wrapper class name. This is useful if a layer/wrapper is replaced
	 * with a new implementation, or moved to a different package.
	 * 
	 * @param oldClassName
	 * @return Replacement layer/wrapper class for the given class name
	 */
	public Class<? extends IPersistentLayer> getReplacementClassFor(String oldClassName)
	{
		return replacementClasses.get(oldClassName);
	}

	/**
	 * Get the layer or wrapper id to use as a replacement for the given
	 * layer/wrapper id. This is useful if a layer/wrapper is replaced with a
	 * new implementation.
	 * 
	 * @param oldId
	 * @return Replacement layer/wrapper id for the given id
	 */
	public String getReplacementIdFor(String oldId)
	{
		return replacementIds.get(oldId);
	}

	/**
	 * Wrap the given legacy World Wind {@link Layer} in the appropriate
	 * associated {@link ILayerWrapper} implementation.
	 * 
	 * @param layer
	 *            Layer to wrap
	 * @return {@link ILayerWrapper} wrapping the given layer
	 */
	public ILayerWrapper wrapLayer(Layer layer)
	{
		try
		{
			ILayerWrapper wrapper = getWrapperForLayer(layer);
			wrapper.setLayer(layer);
			return wrapper;
		}
		catch (Exception e)
		{
			logger.error("Error wrapping layer", e); //$NON-NLS-1$
			return null;
		}
	}

	private ILayerWrapper getWrapperForLayer(Layer layer) throws InstantiationException, IllegalAccessException
	{
		Class<?> layerClass = layer.getClass();
		Map<Class<?>, Integer> distances = new HashMap<Class<?>, Integer>();
		calculateClassDistances(layerClass, 0, distances);
		while (!distances.isEmpty())
		{
			Class<?> closestClass = getClosestClass(distances);
			Class<? extends ILayerWrapper> wrapperClass = layerToWrapper.get(closestClass);
			ILayerWrapper wrapper = wrapperClass.newInstance();
			if (wrapper.supports(layer))
			{
				return wrapper;
			}
			distances.remove(closestClass);
		}
		return null;
	}

	private Class<?> getClosestClass(Map<Class<?>, Integer> distances)
	{
		int minDistance = Integer.MAX_VALUE;
		Class<?> closestClass = null;
		for (Entry<Class<?>, Integer> entry : distances.entrySet())
		{
			if (entry.getValue() < minDistance)
			{
				minDistance = entry.getValue();
				closestClass = entry.getKey();
			}
		}
		return closestClass;
	}

	private void calculateClassDistances(Class<?> c, int distance, Map<Class<?>, Integer> distances)
	{
		if (c == null)
		{
			return;
		}
		if (layerToWrapper.containsKey(c))
		{
			Integer lastDistance = distances.get(c);
			if (lastDistance == null || distance < lastDistance)
			{
				distances.put(c, distance);
			}
		}
		calculateClassDistances(c.getSuperclass(), distance + 1, distances);
		for (Class<?> i : c.getInterfaces())
		{
			calculateClassDistances(i, distance + 1, distances);
		}
	}
}
