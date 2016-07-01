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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.earthsci.worldwind.common.layers.point.types.UrlMarker;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Basic implementation of a {@link Borehole}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BoreholeImpl extends UrlMarker implements Borehole
{
	private final BoreholePath path = new BoreholePathImpl(this);
	private List<BoreholeSample> samples = new ArrayList<BoreholeSample>();
	private List<BoreholeMarker> markers = new ArrayList<BoreholeMarker>();

	public BoreholeImpl(Position position, MarkerAttributes attrs)
	{
		super(position, attrs);

		Validate.notNull(position, "A borehole position is required");
	}

	@Override
	public BoreholePath getPath()
	{
		return path;
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
}
