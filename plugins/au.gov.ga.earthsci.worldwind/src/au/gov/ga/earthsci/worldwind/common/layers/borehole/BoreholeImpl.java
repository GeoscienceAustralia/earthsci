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
	private BoreholePath path = new BoreholePathImpl();
	private List<BoreholeSample> samples = new ArrayList<BoreholeSample>();
	private List<BoreholeMarker> markers = new ArrayList<BoreholeMarker>();

	private FastShape pathShape;
	private FastShape samplesShape;
	private float[] pickingColorBuffer;

	private final PickSupport pickSupport = new PickSupport();

	public BoreholeImpl(BoreholeLayer layer, Position position, MarkerAttributes attrs)
	{
		super(position, attrs);

		Validate.notNull(layer, "A borehole layer is required");
		Validate.notNull(position, "A borehole position is required");

		this.layer = layer;
	}

	@Override
	public BoreholePath getPath()
	{
		return path;
	}

	public void setPath(BoreholePath path)
	{
		this.path = path;
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

	@Override
	public List<BoreholeMarker> getMarkers()
	{
		return markers;
	}

	public void setMarkers(List<BoreholeMarker> markers)
	{
		this.markers = markers;
	}

	/**
	 * Add a position to this borehole's path.
	 * 
	 * @param measuredDepth
	 *            The measured depth of this position
	 * @param position
	 *            The position to add
	 */
	public void addPath(double measuredDepth, Position position)
	{
		path.addPosition(measuredDepth, position);
	}

	/**
	 * Add a sample to this borehole.
	 * 
	 * @param sample
	 *            The sample to add
	 */
	public void addSample(BoreholeSample sample)
	{
		samples.add(sample);
	}

	/**
	 * Add a marker to this borehole.
	 * 
	 * @param marker
	 *            The marker to add
	 */
	public void addMarker(BoreholeMarker marker)
	{
		markers.add(marker);
	}

	/**
	 * Notify this {@link Borehole} that all samples have been added to it, and
	 * it can create it's geometry. This should be called by the
	 * {@link BoreholeLayer} in it's own loadComplete() function.
	 */
	@Override
	public void loadComplete()
	{
		List<Position> positions = new ArrayList<Position>();
		List<Color> colors = new ArrayList<Color>();

		if (path.getPositions().isEmpty() && !getSamples().isEmpty())
		{
			double minDepth = Double.MAX_VALUE;
			double maxDepth = -Double.MAX_VALUE;
			for (BoreholeSample sample : getSamples())
			{
				minDepth = Math.min(minDepth, sample.getDepthFrom());
				minDepth = Math.min(minDepth, sample.getDepthTo());
				maxDepth = Math.max(maxDepth, sample.getDepthFrom());
				maxDepth = Math.max(maxDepth, sample.getDepthTo());
			}
			Position minPosition = getPosition();
			Position maxPosition = new Position(minPosition, minPosition.elevation - (maxDepth - minDepth));
			path.addPosition(minDepth, minPosition);
			path.addPosition(maxDepth, maxPosition);
		}

		for (BoreholeSample sample : getSamples())
		{
			Position sampleTop = path.getPosition(sample.getDepthFrom());
			Position sampleBottom = path.getPosition(sample.getDepthTo());

			positions.add(sampleTop);
			positions.add(sampleBottom);

			Color sampleColor = sample.getColor();
			sampleColor = sampleColor != null ? sampleColor : this.layer.getDefaultSampleColor();
			colors.add(sampleColor);
			colors.add(sampleColor);
		}

		List<Position> pathPositions = new ArrayList<Position>(path.getPositions().values());
		pathShape = new FastShape(pathPositions, GL2.GL_LINE_STRIP);
		pathShape.setColor(Color.LIGHT_GRAY);
		pathShape.setLineWidth(1.0);
		pathShape.setFollowTerrain(layer.isFollowTerrain());

		float[] boreholeColorBuffer = FastShape.color3ToFloats(colors);
		pickingColorBuffer = new float[colors.size() * 3];

		samplesShape = new FastShape(positions, GL2.GL_LINES);
		samplesShape.setColorBuffer(boreholeColorBuffer);
		samplesShape.setFollowTerrain(layer.isFollowTerrain());
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
		if (samplesShape == null)
		{
			return;
		}

		//check if the borehole is within the minimum drawing distance; if not, don't draw
		Extent extent = pathShape.getExtent();
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
			samplesShape.render(dc);
			pathShape.render(dc);
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
				samplesShape.setColor(overallPickColor);
				samplesShape.setColorBufferEnabled(false);
				samplesShape.render(dc);
				samplesShape.setColorBufferEnabled(true);

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
					samplesShape.setPickingColorBuffer(pickingColorBuffer);
					samplesShape.render(dc);
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

	FastShape getPathShape()
	{
		return pathShape;
	}

	FastShape getSamplesShape()
	{
		return samplesShape;
	}
}
