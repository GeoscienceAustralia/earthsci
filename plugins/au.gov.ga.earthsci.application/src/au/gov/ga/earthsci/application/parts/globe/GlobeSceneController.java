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
package au.gov.ga.earthsci.application.parts.globe;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.logging.Level;

import au.gov.ga.earthsci.worldwind.common.render.ExtendedSceneController;

/**
 * SceneController to use for each WorldWindow for the globe part. Provides a
 * separate list of HUD layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GlobeSceneController extends ExtendedSceneController
{
	private final LayerList preLayers = new LayerList();
	private final LayerList postLayers = new LayerList();

	/**
	 * @return Layers to draw before drawing the model's layer list.
	 */
	public LayerList getPreLayers()
	{
		return preLayers;
	}

	/**
	 * @return Layers to draw after drawing the model's layer list.
	 */
	public LayerList getPostLayers()
	{
		return postLayers;
	}

	@Override
	protected void beforePickLayers(DrawContext dc)
	{
		super.beforePickLayers(dc);

		for (Layer layer : preLayers)
		{
			try
			{
				if (layer != null && layer.isPickEnabled())
				{
					dc.setCurrentLayer(layer);
					layer.pick(dc, dc.getPickPoint());
				}
			}
			catch (Exception e)
			{
				String message = Logging.getMessage("SceneController.ExceptionWhilePickingInLayer", //$NON-NLS-1$
						(layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown"))); //$NON-NLS-1$
				Logging.logger().log(Level.SEVERE, message, e);
				// Don't abort; continue on to the next layer.
			}
		}

		dc.setCurrentLayer(null);
	}

	@Override
	protected void afterPickLayers(DrawContext dc)
	{
		super.afterPickLayers(dc);

		for (Layer layer : postLayers)
		{
			try
			{
				if (layer != null && layer.isPickEnabled())
				{
					dc.setCurrentLayer(layer);
					layer.pick(dc, dc.getPickPoint());
				}
			}
			catch (Exception e)
			{
				String message = Logging.getMessage("SceneController.ExceptionWhilePickingInLayer", //$NON-NLS-1$
						(layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown"))); //$NON-NLS-1$
				Logging.logger().log(Level.SEVERE, message, e);
				// Don't abort; continue on to the next layer.
			}
		}

		dc.setCurrentLayer(null);
	}

	@Override
	protected void beforePreRenderLayers(DrawContext dc)
	{
		super.beforePreRenderLayers(dc);

		for (Layer layer : preLayers)
		{
			try
			{
				if (layer != null)
				{
					dc.setCurrentLayer(layer);
					layer.preRender(dc);
				}
			}
			catch (Exception e)
			{
				String message =
						Logging.getMessage("SceneController.ExceptionWhilePreRenderingLayer", (layer != null ? layer //$NON-NLS-1$
								.getClass().getName() : Logging.getMessage("term.unknown"))); //$NON-NLS-1$
				Logging.logger().log(Level.SEVERE, message, e);
				// Don't abort; continue on to the next layer.
			}
		}

		dc.setCurrentLayer(null);
	}

	@Override
	protected void afterPreRenderLayers(DrawContext dc)
	{
		super.afterPreRenderLayers(dc);

		for (Layer layer : postLayers)
		{
			try
			{
				if (layer != null)
				{
					dc.setCurrentLayer(layer);
					layer.preRender(dc);
				}
			}
			catch (Exception e)
			{
				String message =
						Logging.getMessage("SceneController.ExceptionWhilePreRenderingLayer", (layer != null ? layer //$NON-NLS-1$
								.getClass().getName() : Logging.getMessage("term.unknown"))); //$NON-NLS-1$
				Logging.logger().log(Level.SEVERE, message, e);
				// Don't abort; continue on to the next layer.
			}
		}

		dc.setCurrentLayer(null);
	}

	@Override
	protected void beforeDrawLayers(DrawContext dc)
	{
		super.beforeDrawLayers(dc);

		for (Layer layer : preLayers)
		{
			try
			{
				if (layer != null)
				{
					dc.setCurrentLayer(layer);
					layer.render(dc);
				}
			}
			catch (Exception e)
			{
				String message =
						Logging.getMessage("SceneController.ExceptionWhileRenderingLayer", (layer != null ? layer //$NON-NLS-1$
								.getClass().getName() : Logging.getMessage("term.unknown"))); //$NON-NLS-1$
				Logging.logger().log(Level.SEVERE, message, e);
				// Don't abort; continue on to the next layer.
			}
		}

		dc.setCurrentLayer(null);
	}

	@Override
	protected void afterDrawLayers(DrawContext dc)
	{
		super.afterDrawLayers(dc);

		for (Layer layer : postLayers)
		{
			try
			{
				if (layer != null)
				{
					dc.setCurrentLayer(layer);
					layer.render(dc);
				}
			}
			catch (Exception e)
			{
				String message =
						Logging.getMessage("SceneController.ExceptionWhileRenderingLayer", (layer != null ? layer //$NON-NLS-1$
								.getClass().getName() : Logging.getMessage("term.unknown"))); //$NON-NLS-1$
				Logging.logger().log(Level.SEVERE, message, e);
				// Don't abort; continue on to the next layer.
			}
		}

		dc.setCurrentLayer(null);
	}
}
