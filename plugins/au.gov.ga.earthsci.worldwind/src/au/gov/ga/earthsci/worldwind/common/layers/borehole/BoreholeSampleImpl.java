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

import java.awt.Color;

/**
 * Basic implementation of {@link BoreholeSample}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BoreholeSampleImpl implements BoreholeSample
{
	private final Borehole borehole;
	private double depthFrom;
	private double depthTo;
	private Color color;
	private String text;
	private String link;

	public BoreholeSampleImpl(Borehole borehole)
	{
		this.borehole = borehole;
	}

	@Override
	public Borehole getBorehole()
	{
		return borehole;
	}

	@Override
	public double getDepthFrom()
	{
		return depthFrom;
	}

	public void setDepthFrom(double depthFrom)
	{
		this.depthFrom = depthFrom;
	}

	@Override
	public double getDepthTo()
	{
		return depthTo;
	}

	public void setDepthTo(double depthTo)
	{
		this.depthTo = depthTo;
	}

	@Override
	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	@Override
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}
}
