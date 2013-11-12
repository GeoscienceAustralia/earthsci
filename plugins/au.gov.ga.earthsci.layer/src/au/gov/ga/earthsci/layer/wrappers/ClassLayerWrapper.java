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
package au.gov.ga.earthsci.layer.wrappers;

import gov.nasa.worldwind.layers.Layer;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.layer.delegator.LayerDelegator;
import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * {@link ILayerWrapper} that wraps a particular subclass/implementation of
 * {@link Layer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ClassLayerWrapper<L extends Layer> extends LayerDelegator implements ILayerWrapper
{
	private static final Logger logger = LoggerFactory.getLogger(ClassLayerWrapper.class);
	private final static String LAYER_CLASS_ATTRIBUTE = "layerClass"; //$NON-NLS-1$

	/**
	 * @return Type of the layer instance that this class wraps
	 */
	protected abstract Class<L> getWrappedLayerClass();

	/**
	 * Load the properties for the given layer from XML. The XML element
	 * provided is the same element provided to the
	 * {@link #save(Layer, Element)} method.
	 * 
	 * @param layer
	 *            Layer to set loaded properties on
	 * @param element
	 *            XML element to load properties from
	 */
	protected abstract void load(L layer, Element element);

	/**
	 * Save the properties of the given layer to XML.
	 * 
	 * @param layer
	 *            Layer to save (same as {@link #getLayer()})
	 * @param element
	 *            XML element to save layer properties to
	 */
	protected abstract void save(L layer, Element element);

	/**
	 * Create a new instance of the layer wrapped by this class. Default
	 * implementation calls <code>layerClass.newInstance()</code>.
	 * 
	 * @param element
	 *            XML element being loaded from
	 * @param layerClass
	 *            Class of the layer required to be instantiated
	 * @return New instance of the layer wrapped by this class
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected L createLayer(Element element, Class<? extends L> layerClass) throws InstantiationException,
			IllegalAccessException
	{
		return layerClass.newInstance();
	}

	@Override
	public final void load(Element parent)
	{
		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends L> layerClass =
					(Class<? extends L>) Class.forName(parent.getAttribute(LAYER_CLASS_ATTRIBUTE));
			if (!getWrappedLayerClass().isAssignableFrom(layerClass))
			{
				layerClass = getWrappedLayerClass();
			}
			L layer = createLayer(parent, layerClass);
			setLayer(layer);
			load(layer, parent);
		}
		catch (Exception e)
		{
			logger.error("Error loading layer", e); //$NON-NLS-1$
		}
	}

	@Override
	public final void save(Element parent)
	{
		L layer = getLayer();
		save(layer, parent);
		parent.setAttribute(LAYER_CLASS_ATTRIBUTE, layer.getClass().getName());
	}

	@Override
	public void initialize(ILayerNode node, IEclipseContext context)
	{
	}

	@Override
	public boolean isLoading()
	{
		return false;
	}

	@Override
	public boolean supports(Layer layer)
	{
		return getLayerClass().isAssignableFrom(layer.getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	public L getLayer()
	{
		return (L) super.getLayer();
	}

	@Override
	public void setLayer(Layer layer)
	{
		if (!supports(layer))
		{
			throw new IllegalArgumentException();
		}
		super.setLayer(layer);
	}
}
