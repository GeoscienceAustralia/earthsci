/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.retrieve;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Tile;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import au.gov.ga.earthsci.worldwind.common.retrieve.ExtendedRetrievalService.RetrievalListener;

/**
 * {@link RetrievalListener} implementation that displays polylines on the
 * earth's surface when the {@link ExtendedRetrievalService} downloads surface
 * tiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PolylineLayerRetrievalListener extends RenderableLayer implements RetrievalListener
{
	private static final Color COLOR = new Color(1f, 0f, 0f, 0.5f);

	private final Map<Retriever, SectorPolyline> retrievingLines = new HashMap<Retriever, SectorPolyline>();

	@Override
	public void beforeRetrieve(Retriever retriever)
	{
		Tile tile = RetrievalListenerHelper.getTile(retriever);
		if (tile != null)
		{
			Sector sector = tile.getSector();
			SectorPolyline s = new SectorPolyline(sector);
			s.setColor(COLOR);
			s.setLineWidth(2.0);
			s.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);

			synchronized (retrievingLines)
			{
				retrievingLines.put(retriever, s);
			}

			addRenderable(s);
		}
	}

	@Override
	public void afterRetrieve(Retriever retriever)
	{
		SectorPolyline s = retrievingLines.remove(retriever);
		if (s != null)
		{
			removeRenderable(s);
			firePropertyChange(AVKey.LAYER, null, this);
		}
	}

	@Override
	protected synchronized void doRender(DrawContext dc)
	{
		super.doRender(dc);
	}

	@Override
	public synchronized void addRenderable(Renderable renderable)
	{
		super.addRenderable(renderable);
	}

	@Override
	public synchronized void removeRenderable(Renderable renderable)
	{
		super.removeRenderable(renderable);
	}
}
