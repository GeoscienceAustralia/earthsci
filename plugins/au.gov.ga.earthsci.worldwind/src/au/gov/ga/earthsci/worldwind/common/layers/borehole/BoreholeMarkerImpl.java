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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;

import au.gov.ga.earthsci.worldwind.common.layers.point.types.UrlMarker;
import au.gov.ga.earthsci.worldwind.common.render.DiskMarkerAttributes;

/**
 * Basic implementation of a {@link BoreholeMarker}.
 *
 * @author Michael de Hoog
 */
public class BoreholeMarkerImpl extends UrlMarker implements BoreholeMarker
{
	private final Borehole borehole;
	private double depth;

	public BoreholeMarkerImpl(Borehole borehole, Position position)
	{
		this(borehole, position, new DiskMarkerAttributes());
	}

	public BoreholeMarkerImpl(Borehole borehole, Position position, MarkerAttributes markerAttributes)
	{
		super(position, markerAttributes);
		this.borehole = borehole;
	}

	@Override
	public Borehole getBorehole()
	{
		return borehole;
	}

	@Override
	public double getDepth()
	{
		return depth;
	}

	public void setDepth(double depth)
	{
		this.depth = depth;
	}

	@Override
	public Angle getAzimuth()
	{
		return getHeading();
	}

	public void setAzimuth(Angle azimuth)
	{
		setHeading(azimuth);
	}

	@Override
	public Angle getDip()
	{
		return getPitch();
	}

	public void setDip(Angle dip)
	{
		setPitch(dip);
	}

	@Override
	public Color getColor()
	{
		return attributes.getMaterial().getDiffuse();
	}

	public void setColor(Color color)
	{
		attributes.setMaterial(new Material(color));
	}

	@Override
	public String getText()
	{
		return getTooltipText();
	}

	public void setText(String text)
	{
		setTooltipText(text);
	}

	@Override
	public String getLink()
	{
		return getUrl();
	}

	public void setLink(String link)
	{
		setUrl(link);
	}
}
