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

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.earthsci.common.persistence.IPersistentAdapter;
import au.gov.ga.earthsci.common.util.Util;

/**
 * {@link IPersistentAdapter} implementation for {@link ILayer} instances. Calls
 * the {@link ILayer#load} and {@link ILayer#save} methods to load/save the XML
 * to the persistent element.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerPersistentAdapter implements IPersistentAdapter<ILayer>
{
	private static final Logger logger = LoggerFactory.getLogger(LayerPersistentAdapter.class);
	private final static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private final static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	@Override
	public void toXML(ILayer layer, Element element, URI context)
	{
		String id, className;
		if (layer instanceof MissingLayer)
		{
			MissingLayer missing = (MissingLayer) layer;
			id = missing.id;
			className = missing.className;
		}
		else
		{
			id = ExtensionManager.getInstance().getIdForLayerOrWrapper(layer);
			className = layer.getClass().getName();
		}
		if (!Util.isEmpty(id))
		{
			element.setAttribute(ID_ATTRIBUTE, id);
		}
		else if (!Util.isEmpty(className))
		{
			element.setAttribute(CLASS_ATTRIBUTE, className);
		}
		layer.save(element);
	}

	@Override
	public ILayer fromXML(Element element, URI context)
	{
		String id = element.getAttribute(ID_ATTRIBUTE);
		String className = element.getAttribute(CLASS_ATTRIBUTE);
		try
		{
			Class<? extends ILayer> c;
			if (!Util.isEmpty(id))
			{
				c = ExtensionManager.getInstance().getLayerOrWrapperForId(id);
				if (c == null)
				{
					throw new IllegalArgumentException("Layer or wrapper class not found for id '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else if (!Util.isEmpty(className))
			{
				c = ExtensionManager.getInstance().getReplacementFor(className);
				if (c == null)
				{
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Class<? extends ILayer> cl = (Class) Class.forName(className);
					c = cl;
				}
			}
			else
			{
				throw new IllegalArgumentException("No layer id or class specified"); //$NON-NLS-1$
			}
			ILayer layer = c.newInstance();
			layer.load(element);

			//check if a wrapped layer should actually be wrapped by a different wrapper
			if (layer instanceof ILayerWrapper)
			{
				ILayerWrapper wrapper = (ILayerWrapper) layer;
				ILayerWrapper otherWrapper = ExtensionManager.getInstance().wrapLayer(wrapper.getLayer());
				if (!otherWrapper.getClass().isAssignableFrom(wrapper.getClass()))
				{
					layer = otherWrapper;
				}
			}

			return layer;
		}
		catch (Exception e)
		{
			logger.error("Error creating Layer from XML", e); //$NON-NLS-1$
			return new MissingLayer(id, className, element);
		}
	}

	/**
	 * Basic {@link ILayer} implementation used for storage of the layer XML
	 * when the layer class cannot be found.
	 */
	private static class MissingLayer extends AbstractLayer implements ILayer
	{
		private final String id;
		private final String className;
		private final NodeList children;

		public MissingLayer(String id, String className, Element parent)
		{
			this.id = id;
			this.className = className;
			this.children = parent.getChildNodes();
		}

		@Override
		public void load(Element parent)
		{
		}

		@Override
		public void save(Element parent)
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);
				Node imported = parent.getOwnerDocument().importNode(child, true);
				parent.appendChild(imported);
			}
		}

		@Override
		public boolean isLoading()
		{
			return false;
		}

		@Override
		protected void doRender(DrawContext dc)
		{
		}
	}
}
