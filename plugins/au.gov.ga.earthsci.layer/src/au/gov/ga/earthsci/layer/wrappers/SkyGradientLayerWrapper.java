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
package au.gov.ga.earthsci.layer.wrappers;

import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.transform.TransformSkyGradientLayer;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * {@link ILayerWrapper} for {@link SkyGradientLayer}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SkyGradientLayerWrapper extends ClassLayerWrapper<TransformSkyGradientLayer>
{
	private final static String ATMOSPHERE_THICKNESS_ELEMENT = "AtmosphereThickness"; //$NON-NLS-1$
	private final static String HORIZON_COLOR_ELEMENT = "HorizonColor"; //$NON-NLS-1$
	private final static String ZENITH_COLOR_ELEMENT = "ZenithColor"; //$NON-NLS-1$

	@Override
	protected Class<TransformSkyGradientLayer> getWrappedLayerClass()
	{
		return TransformSkyGradientLayer.class;
	}

	@Override
	protected void load(TransformSkyGradientLayer layer, Element element)
	{
		XPath xpath = WWXML.makeXPath();

		Double atmosphereThickness = WWXML.getDouble(element, ATMOSPHERE_THICKNESS_ELEMENT, xpath);
		if (atmosphereThickness != null)
		{
			layer.setAtmosphereThickness(atmosphereThickness);
		}

		Color horizonColor = WWXML.getColor(element, HORIZON_COLOR_ELEMENT, xpath);
		if (horizonColor != null)
		{
			layer.setHorizonColor(horizonColor);
		}

		Color zenithColor = WWXML.getColor(element, ZENITH_COLOR_ELEMENT, xpath);
		if (zenithColor != null)
		{
			layer.setZenithColor(zenithColor);
		}
	}

	@Override
	protected void save(TransformSkyGradientLayer layer, Element element)
	{
		WWXML.appendDouble(element, ATMOSPHERE_THICKNESS_ELEMENT, layer.getAtmosphereThickness());
		XMLUtil.appendColor(element, HORIZON_COLOR_ELEMENT, layer.getHorizonColor());
		XMLUtil.appendColor(element, ZENITH_COLOR_ELEMENT, layer.getZenithColor());
	}

	public double getAtmosphereThickness()
	{
		return getLayer().getAtmosphereThickness();
	}

	public void setAtmosphereThickness(double atmosphereThickness)
	{
		getLayer().setAtmosphereThickness(atmosphereThickness);
	}

	public Color getHorizonColor()
	{
		return getLayer().getHorizonColor();
	}

	public void setHorizonColor(Color horizonColor)
	{
		getLayer().setHorizonColor(horizonColor);
	}

	public Color getZenithColor()
	{
		return getLayer().getZenithColor();
	}

	public void setZenithColor(Color zenithColor)
	{
		getLayer().setZenithColor(zenithColor);
	}
}
