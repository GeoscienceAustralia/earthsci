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
package au.gov.ga.earthsci.bookmark.properties.layer;

import gov.nasa.worldwind.layers.Layer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import au.gov.ga.earthsci.bookmark.AbstractBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.common.math.vector.Vector2;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * An {@link IBookmarkPropertyAnimator} that can animate the world layer state
 * between the two given property states.
 * <p/>
 * Animation semantics are as follows:
 * <ol>
 * <li>Layer matching is done solely on layer URI
 * <li>Layers that appear in the current world model but neither the start nor
 * end property are animated between their current opacity value and 0.0 (off)
 * <li>Layers that appear in the start and/or end properties but NOT in the
 * current world model are ignored
 * <li>Layers that appear in both start and end properties AND in the current
 * world model are animated between the opacities stored in the start and end
 * properties
 * <li>Layers that appear in the start property but NOT the end property are
 * animated between the start property opacity and 0.0 (off)
 * <li>Layers that appear in the end property but NOT the start property are
 * animated between their current opacity and the end property opacity
 * <li>A layer which is disabled in the current world model will be assigned an
 * opacity value of 0.0 where the world value is used above
 * </ol>
 * 
 * The above rules are enumerated in the matrix below (W = world state, S =
 * Start property state, E = End property state, O = Off, I = Ignore).
 * 
 * <pre>
 * W    S    E   |  Animate
 * --------------------------
 * 1    1    1   |  S -> E
 * 1    1    0   |  S -> O
 * 1    0    1   |  W -> E
 * 1    0    0   |  W -> O
 * 0    1    1   |  I
 * 0    1    0   |  I
 * 0    0    1   |  I
 * 0    0    0   |  I
 * 
 * </pre>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayersPropertyAnimator extends AbstractBookmarkPropertyAnimator
{

	private static final double DISABLED_OPACITY_THRESHOLD = 0.001;

	private final ITreeModel model;

	private final Map<Layer, Vector2> animationVectors;
	private boolean initialised = false;

	public LayersPropertyAnimator(final ITreeModel model, final LayersProperty start, final LayersProperty end,
			long duration)
	{
		super(start, end, duration);
		this.model = model;
		this.animationVectors = new ConcurrentHashMap<Layer, Vector2>();
	}

	@Override
	public boolean isInitialised()
	{
		return initialised;
	}

	@Override
	public void init()
	{
		super.init();

		animationVectors.clear();

		for (Layer l : model.getLayers())
		{
			if (!(l instanceof LayerNode))
			{
				continue;
			}

			LayerNode layerNode = (LayerNode) l;

			animationVectors.put(layerNode, new Vector2(getStartOpacity(layerNode), getEndOpacity(layerNode)));
		}
		initialised = true;
	}

	@Override
	public void applyFrame()
	{
		super.applyFrame();

		for (Entry<Layer, Vector2> e : animationVectors.entrySet())
		{
			Vector2 v = e.getValue();
			e.getKey().setOpacity(Util.mixDouble(getCurrentTimeAsPercent(), v.x, v.y));
			e.getKey().setEnabled(e.getKey().getOpacity() > DISABLED_OPACITY_THRESHOLD);
		}
	}

	private double getWorldOpacity(LayerNode l)
	{
		if (l.isEnabled())
		{
			return l.getOpacity();
		}
		return 0.0;
	}

	private Double getStartOpacity(LayerNode l)
	{
		if (isInStartProperty(l))
		{
			return getStartProperty().getLayerState().get(l.getURI());
		}
		return getWorldOpacity(l);
	}

	private Double getEndOpacity(LayerNode l)
	{
		if (isInEndProperty(l))
		{
			return getEndProperty().getLayerState().get(l.getURI());
		}
		return 0.0;
	}

	private boolean isInStartProperty(LayerNode l)
	{
		return getStartProperty().getLayerState().containsKey(l.getURI());
	}

	private LayersProperty getStartProperty()
	{
		return (LayersProperty) getStart();
	}

	private boolean isInEndProperty(LayerNode l)
	{
		return getEndProperty().getLayerState().containsKey(l.getURI());
	}

	private LayersProperty getEndProperty()
	{
		return (LayersProperty) getEnd();
	}
}
