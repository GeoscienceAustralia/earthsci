/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

import java.util.Iterator;

import javax.media.opengl.GL2;

/**
 * {@link MarkerRenderer} that supports picking of subsurface markers.
 *
 * @author Michael de Hoog
 */
public class DeepPickingMarkerRenderer extends MarkerRenderer
{
	private boolean oldDeepPicking;
	private boolean drawImmediately;
	private MarkerAttributes previousAttributes;

	@Override
	protected boolean intersectsFrustum(DrawContext dc, Vec4 point, double radius)
	{
		//use the same test for drawing and picking (see superclass' method)
		return dc.getView().getFrustumInModelCoordinates().contains(point);
	}

	@Override
	protected void begin(DrawContext dc)
	{
		if (dc.isPickingMode())
		{
			oldDeepPicking = dc.isDeepPickingEnabled();
			dc.setDeepPickingEnabled(true);
		}
		super.begin(dc);
	}

	@Override
	protected void end(DrawContext dc)
	{
		super.end(dc);
		if (dc.isPickingMode())
		{
			dc.setDeepPickingEnabled(oldDeepPicking);
		}
	}

	public boolean isDrawImmediately()
	{
		return drawImmediately;
	}

	public void setDrawImmediately(boolean drawImmediately)
	{
		this.drawImmediately = drawImmediately;
	}

	@Override
	protected void draw(final DrawContext dc, Iterable<Marker> markers)
	{
		if (isDrawImmediately())
		{
			drawImmediately(dc, markers);
		}
		else
		{
			super.draw(dc, markers);
		}
	}

	protected void drawImmediately(DrawContext dc, Iterable<Marker> markers)
	{
		Layer parentLayer = dc.getCurrentLayer();
		try
		{
			begin(dc);

			Iterator<Marker> markerIterator = markers.iterator();
			for (int index = 0; markerIterator.hasNext(); index++)
			{
				Marker marker = markerIterator.next();
				Position pos = marker.getPosition();
				Vec4 point = this.computeSurfacePoint(dc, pos);
				double radius = this.computeMarkerRadius(dc, point, marker);
				if (!intersectsFrustum(dc, point, radius))
				{
					continue;
				}
				drawMarker(dc, index, marker, point, radius);
			}
		}
		finally
		{
			end(dc);
			if (dc.isPickingMode())
			{
				this.pickSupport.resolvePick(dc, dc.getPickPoint(), parentLayer); // Also clears the pick list.
			}
		}
	}

	/*
	 * Same as superclass' method (copied because private)
	 */
	private void drawMarker(DrawContext dc, int index, Marker marker, Vec4 point, double radius)
	{
		// This method is called from OrderedMarker's render and pick methods. We don't perform culling here, because
		// the marker has already been culled against the appropriate frustum prior adding OrderedMarker to the draw
		// context.

		if (dc.isPickingMode())
		{
			java.awt.Color color = dc.getUniquePickColor();
			int colorCode = color.getRGB();
			PickedObject po = new PickedObject(colorCode, marker, marker.getPosition(), false);
			po.setValue(AVKey.PICKED_OBJECT_ID, index);
			if (this.isEnablePickSizeReturn())
			{
				po.setValue(AVKey.PICKED_OBJECT_SIZE, 2 * radius);
			}
			this.pickSupport.addPickableObject(po);
			GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
			gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
		}

		MarkerAttributes attrs = marker.getAttributes();
		if (attrs != this.previousAttributes) // equality is intentional to avoid constant equals() calls
		{
			attrs.apply(dc);
			this.previousAttributes = attrs;
		}

		marker.render(dc, point, radius);
	}
}
