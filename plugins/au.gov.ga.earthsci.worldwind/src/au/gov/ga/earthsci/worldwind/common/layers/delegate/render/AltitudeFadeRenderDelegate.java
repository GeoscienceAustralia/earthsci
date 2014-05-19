/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRenderDelegate;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * {@link IRenderDelegate} that fades layers according to camera altitude.
 * <p/>
 * <code>&lt;Delegate&gt;AltitudeFade(start,end)&lt;/Delegate&gt;</code></br>
 * <ul>
 * <li>Start = eye altitude at which to begin fading out (above this opacity =
 * 1.0)
 * <li>End = eye altitude at which to end fading out (below this opacity = 0.0)
 * </ul>
 * Start and end values can be swapped to fade in instead of out.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class AltitudeFadeRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "AltitudeFade";
	protected final double altitudeStart;
	protected final double altitudeEnd;

	protected Layer layer;
	protected double oldOpacity;

	private AltitudeFadeRenderDelegate()
	{
		this(100000, 10000);
	}

	public AltitudeFadeRenderDelegate(double altitudeStart, double altitudeEnd)
	{
		this.altitudeStart = altitudeStart;
		this.altitudeEnd = altitudeEnd;
	}

	@Override
	public void preRender(DrawContext dc)
	{
		layer = dc.getCurrentLayer();
		if (layer == null)
		{
			return;
		}
		oldOpacity = layer.getOpacity();

		Vec4 eyePoint = dc.getView().getEyePoint();
		double eyeMagnitude = eyePoint.getLength3() - dc.getGlobe().getRadius();
		double opacity = (eyeMagnitude - altitudeEnd) / (altitudeStart - altitudeEnd);
		opacity = Util.clamp(opacity, 0.0, 1.0);
		layer.setOpacity(opacity);
	}

	@Override
	public void postRender(DrawContext dc)
	{
		if (layer == null)
		{
			return;
		}
		layer.setOpacity(oldOpacity);
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + altitudeStart + "," + altitudeEnd + ")";
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\(([\\d.\\-]+),([\\d.\\-]+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				double altitudeStart = Double.parseDouble(matcher.group(1));
				double altitudeEnd = Double.parseDouble(matcher.group(2));
				return new AltitudeFadeRenderDelegate(altitudeStart, altitudeEnd);
			}
			return new AltitudeFadeRenderDelegate();
		}
		return null;
	}
}
