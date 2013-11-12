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
package au.gov.ga.earthsci.layer.delegator;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.layer.IPersistentLayer;
import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * Implementation of the {@link ILayerDelegator} interface, delegating to
 * {@link IPersistentLayer} instances.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PersistentLayerDelegator extends AbstractLayerDelegator<IPersistentLayer> implements IPersistentLayer
{
	@Override
	public Class<IPersistentLayer> getLayerClass()
	{
		return IPersistentLayer.class;
	}

	@Override
	protected IPersistentLayer createDummyLayer()
	{
		return new DummyLayer();
	}

	@Override
	protected boolean isDummyLayer(IPersistentLayer layer)
	{
		return layer instanceof DummyLayer;
	}

	////////////////
	// Delegation //
	////////////////

	@Override
	public boolean isLoading()
	{
		return getLayer().isLoading();
	}

	@Override
	public void save(Element parent)
	{
		getLayer().save(parent);
	}

	@Override
	public void load(Element parent)
	{
		getLayer().load(parent);
	}

	@Override
	public void initialize(ILayerNode node, IEclipseContext context)
	{
		getLayer().initialize(node, context);
	}

	/**
	 * Dummy layer that implements {@link IPersistentLayer}, for returning from
	 * the {@link AbstractLayerDelegator#createDummyLayer()} method.
	 */
	private static class DummyLayer extends AbstractLayer implements IPersistentLayer
	{
		@Override
		protected void doRender(DrawContext dc)
		{
		}

		@Override
		public boolean isLoading()
		{
			return false;
		}

		@Override
		public void save(Element parent)
		{
		}

		@Override
		public void load(Element parent)
		{
		}

		@Override
		public void initialize(ILayerNode node, IEclipseContext context)
		{
		}
	}
}
