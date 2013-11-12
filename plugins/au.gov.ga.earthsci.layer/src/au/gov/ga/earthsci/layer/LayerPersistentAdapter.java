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

import java.lang.reflect.Constructor;
import java.net.URI;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.earthsci.common.persistence.IPersistentAdapter;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.core.model.IModelStatus;
import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.layer.wrappers.ILayerWrapper;

/**
 * {@link IPersistentAdapter} implementation for {@link IPersistentLayer}
 * instances. Calls the {@link IPersistentLayer#load} and
 * {@link IPersistentLayer#save} methods to load/save the XML to the persistent
 * element.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerPersistentAdapter implements IPersistentAdapter<IPersistentLayer>
{
	private static final Logger logger = LoggerFactory.getLogger(LayerPersistentAdapter.class);
	private final static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private final static String BUNDLE_ATTRIBUTE = "bundle"; //$NON-NLS-1$
	private final static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	@Override
	public void toXML(IPersistentLayer layer, Element element, URI context)
	{
		String id, bundleName, className;
		if (layer instanceof MissingLayer)
		{
			MissingLayer missing = (MissingLayer) layer;
			id = missing.id;
			bundleName = missing.bundleName;
			className = missing.className;
		}
		else
		{
			id = ExtensionManager.getInstance().getIdForLayerOrWrapper(layer);
			bundleName = FrameworkUtil.getBundle(layer.getClass()).getSymbolicName();
			className = layer.getClass().getName();
		}
		if (!Util.isEmpty(id))
		{
			element.setAttribute(ID_ATTRIBUTE, id);
		}
		else if (!Util.isEmpty(bundleName) && !Util.isEmpty(className))
		{
			element.setAttribute(BUNDLE_ATTRIBUTE, bundleName);
			element.setAttribute(CLASS_ATTRIBUTE, className);
		}
		layer.save(element);
	}

	@Override
	public IPersistentLayer fromXML(Element element, URI context)
	{
		String id = element.getAttribute(ID_ATTRIBUTE);
		String className = element.getAttribute(CLASS_ATTRIBUTE);
		String bundleName = element.getAttribute(BUNDLE_ATTRIBUTE);
		try
		{
			Class<? extends IPersistentLayer> c;
			if (!Util.isEmpty(id))
			{
				String replacement = ExtensionManager.getInstance().getReplacementIdFor(id);
				if (!Util.isEmpty(replacement))
				{
					id = replacement;
				}
				c = ExtensionManager.getInstance().getLayerOrWrapperForId(id);
				if (c == null)
				{
					throw new IllegalArgumentException("Layer or wrapper class not found for id '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else if (!Util.isEmpty(className))
			{
				c = ExtensionManager.getInstance().getReplacementClassFor(className);
				if (c == null)
				{
					Bundle bundle = Platform.getBundle(bundleName);
					@SuppressWarnings({ "unchecked" })
					Class<? extends IPersistentLayer> cl =
							(Class<? extends IPersistentLayer>) bundle.loadClass(className);
					c = cl;
				}
			}
			else
			{
				throw new IllegalArgumentException("No layer id or class specified"); //$NON-NLS-1$
			}
			Constructor<? extends IPersistentLayer> constructor = c.getDeclaredConstructor();
			constructor.setAccessible(true);
			IPersistentLayer layer = constructor.newInstance();
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
			return new MissingLayer(id, bundleName, className, element);
		}
	}

	/**
	 * Basic {@link IPersistentLayer} implementation used for storage of the
	 * layer XML when the layer class cannot be found.
	 */
	private static class MissingLayer extends AbstractLayer implements IPersistentLayer
	{
		private final String id;
		private final String bundleName;
		private final String className;
		private final Element parent;
		private IModelStatus status;

		public MissingLayer(String id, String bundleName, String className, Element parent)
		{
			this.id = id;
			this.bundleName = bundleName;
			this.className = className;
			this.parent = parent;

			String message =
					!Util.isEmpty(id) ? "Layer not found with id: " + id : "Layer class not found: " + className; //$NON-NLS-1$ //$NON-NLS-2$
			this.status = ModelStatus.error(message, null);
		}

		@Override
		public void load(Element parent)
		{
		}

		@Override
		public void save(Element parent)
		{
			NodeList children = this.parent.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node child = children.item(i);
				Node imported = parent.getOwnerDocument().importNode(child, true);
				parent.appendChild(imported);
			}
			NamedNodeMap attributes = this.parent.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++)
			{
				Node attribute = attributes.item(i);
				parent.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
			}
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
		protected void doRender(DrawContext dc)
		{
		}

		@Override
		public String getName()
		{
			return status.getMessage();
		}
	}
}
