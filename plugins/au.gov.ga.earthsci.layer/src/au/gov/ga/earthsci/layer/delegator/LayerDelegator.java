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
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;

/**
 * Basic implementation of the {@link ILayerDelegator} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerDelegator extends AbstractLayerDelegator<Layer>
{
	@Override
	public Class<Layer> getLayerClass()
	{
		return Layer.class;
	}

	@Override
	protected Layer createDummyLayer()
	{
		return new DummyLayer();
	}

	@Override
	protected boolean isDummyLayer(Layer layer)
	{
		return layer instanceof DummyLayer;
	}

	/**
	 * Dummy layer for returning from the
	 * {@link AbstractLayerDelegator#createDummyLayer()} method.
	 */
	private static class DummyLayer extends AbstractLayer
	{
		@Override
		protected void doRender(DrawContext dc)
		{
		}
	}
}
