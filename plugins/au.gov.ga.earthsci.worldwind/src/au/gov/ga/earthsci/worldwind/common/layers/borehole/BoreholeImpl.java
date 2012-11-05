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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.layers.point.types.UrlMarker;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Basic implementation of a {@link Borehole}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BoreholeImpl extends UrlMarker implements Borehole, Renderable
{
	private final BoreholeLayer layer;
	private final Object sampleLock = new Object();
	private List<BoreholeSample> samples = new ArrayList<BoreholeSample>();

	private FastShape fastShape;
	private FastShape centreline;
	private float[] boreholeColorBuffer;
	private float[] pickingColorBuffer;

	private final PickSupport pickSupport = new PickSupport();

	public BoreholeImpl(BoreholeLayer layer, Position position, MarkerAttributes attrs)
	{
		super(position, attrs);
		
		Validate.notNull(layer, "A borehole layer is required");
		Validate.notNull(position, "A marker position is required");
		
		this.layer = layer;
	}

	@Override
	public List<BoreholeSample> getSamples()
	{
		return samples;
	}

	public void setSamples(List<BoreholeSample> samples)
	{
		this.samples = samples;
	}

	/**
	 * Add a sample to this borehole.
	 * <p/>
	 * Threadsafe.
	 * 
	 * @param sample The sample to add. Null samples will be ignored.
	 * 
	 * @throw IllegalArgumentException If the provided sample is from the wrong borehole.
	 */
	public void addSample(BoreholeSample sample)
	{
		if (sample == null)
		{
			return;
		}
		
		if (sample.getBorehole() != this)
		{
			throw new IllegalArgumentException("Sample added from wrong borehole: " + sample.getBorehole() == null ? "null" : sample.getBorehole().toString());
		}
		
		synchronized (sampleLock)
		{
			samples.add(sample);
		}
	}

	/**
	 * Notify this {@link Borehole} that all samples have been added to it, and
	 * it can create it's geometry. This should be called by the
	 * {@link BoreholeLayer} in it's own loadComplete() function.
	 */
	public void loadComplete()
	{
		List<Position> positions = new ArrayList<Position>();
		List<Color> colors = new ArrayList<Color>();

		List<Position> centrelinePositions = new ArrayList<Position>();
		
		double latitude = getPosition().getLatitude().degrees;
		double longitude = getPosition().getLongitude().degrees;
		
		for (BoreholeSample sample : getSamples())
		{
			Position sampleTop = Position.fromDegrees(latitude, longitude, -sample.getDepthFrom());
			Position sampleBottom = Position.fromDegrees(latitude, longitude, -sample.getDepthTo());

			positions.add(sampleTop);
			positions.add(sampleBottom);
			
			Color sampleColor = sample.getColor() == null ? this.layer.getDefaultSampleColor() : sample.getColor();
			colors.add(sampleColor);
			colors.add(sampleColor);
		}
		
		if (!positions.isEmpty())
		{
			centrelinePositions.add(getPosition());
			centrelinePositions.add(positions.get(positions.size() - 1));
		}
		
		boreholeColorBuffer = FastShape.color3ToFloats(colors);
		pickingColorBuffer = new float[colors.size() * 3];

		fastShape = new FastShape(positions, GL2.GL_LINES);
		fastShape.setColorBuffer(boreholeColorBuffer);
		fastShape.setFollowTerrain(true);
		
		centreline = new FastShape(centrelinePositions, GL2.GL_LINES);
		centreline.setColor(Color.LIGHT_GRAY);
		centreline.setLineWidth(1.0);
		centreline.setFollowTerrain(true);
	}

	@Override
	public String getText()
	{
		return getTooltipText();
	}

	@Override
	public String getLink()
	{
		return getUrl();
	}

	@Override
	public void render(DrawContext dc)
	{
		if (fastShape == null)
		{
			return;
		}

		//check if the borehole is within the minimum drawing distance; if not, don't draw
		Extent extent = fastShape.getExtent();
		if (extent != null && layer.getMinimumDistance() != null)
		{
			double distanceToEye = extent.getCenter().distanceTo3(dc.getView().getEyePoint()) - extent.getRadius();
			if (distanceToEye > layer.getMinimumDistance())
			{
				return;
			}
		}

		if (!dc.isPickingMode())
		{
			fastShape.render(dc);
			centreline.render(dc);
		}
		else
		{
			//Don't calculate the picking buffer if the shape isn't going to be rendered anyway.
			//This check is also performed in the shape's render() function, so don't do it above.
			if (extent != null && !dc.getView().getFrustumInModelCoordinates().intersects(extent))
			{
				return;
			}

			boolean oldDeepPicking = dc.isDeepPickingEnabled();
			try
			{
				//deep picking needs to be enabled, because boreholes are below the surface
				dc.setDeepPickingEnabled(true);
				pickSupport.beginPicking(dc);

				//First pick on the entire object by setting the shape to a single color.
				//This will determine if we have to go further and pick individual samples.
				Color overallPickColor = dc.getUniquePickColor();
				pickSupport.addPickableObject(overallPickColor.getRGB(), this, getPosition());
				fastShape.setColor(overallPickColor);
				fastShape.setColorBufferEnabled(false);
				fastShape.render(dc);
				fastShape.setColorBufferEnabled(true);

				PickedObject object = pickSupport.getTopObject(dc, dc.getPickPoint());
				pickSupport.clearPickList();

				if (object != null && object.getObject() == this)
				{
					//This borehole has been picked; now try picking the samples individually

					//Put unique pick colours into the pickingColorBuffer (2 per sample)
					int i = 0;
					for (BoreholeSample sample : getSamples())
					{
						Color color = dc.getUniquePickColor();
						pickSupport.addPickableObject(color.getRGB(), sample, getPosition());
						for (int j = 0; j < 2; j++)
						{
							pickingColorBuffer[i++] = color.getRed() / 255f;
							pickingColorBuffer[i++] = color.getGreen() / 255f;
							pickingColorBuffer[i++] = color.getBlue() / 255f;
						}
					}

					//render the shape with the pickingColorBuffer, and then resolve the pick
					fastShape.setPickingColorBuffer(pickingColorBuffer);
					fastShape.render(dc);
					pickSupport.resolvePick(dc, dc.getPickPoint(), layer);
				}
			}
			finally
			{
				pickSupport.endPicking(dc);
				dc.setDeepPickingEnabled(oldDeepPicking);
			}
		}
	}
	
	FastShape getSamplesShape()
	{
		return fastShape;
	}
	
	FastShape getCentrelineShape()
	{
		return centreline;
	}
}
