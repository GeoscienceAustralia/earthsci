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
package au.gov.ga.earthsci.worldwind.common.util.exaggeration;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The default implementation of the {@link VerticalExaggerationService} that simply uses the global
 * vertical exaggeration as provided by the supplied {@link DrawContext}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DefaultVerticalExaggerationServiceImpl implements VerticalExaggerationService
{
	
	private Map<Object, Double> marks = new WeakHashMap<Object, Double>();  
	
	@Override
	public double applyVerticalExaggeration(DrawContext dc, double elevation)
	{
		return dc.getVerticalExaggeration() * elevation;
	}

	@Override
	public double unapplyVerticalExaggeration(DrawContext dc, double exaggeratedElevation)
	{
		return exaggeratedElevation / dc.getVerticalExaggeration();
	}
	
	@Override
	public double getGlobalVerticalExaggeration(DrawContext dc)
	{
		return dc.getVerticalExaggeration();
	}
	
	@Override
	public double getUnexaggeratedElevation(DrawContext dc, Angle latitude, Angle longitude)
	{
		return dc.getGlobe().getElevation(latitude, longitude);
	}
	
	@Override
	public void markVerticalExaggeration(Object key, DrawContext dc)
	{
		marks.put(key, dc.getVerticalExaggeration());
	}
	
	@Override
	public boolean isVerticalExaggerationChanged(Object key, DrawContext dc)
	{
		if (!marks.containsKey(key))
		{
			return true;
		}
		return marks.get(key) != dc.getVerticalExaggeration();
	}
	
	@Override
	public void clearMark(Object key)
	{
		marks.remove(key);
	}
	
	@Override
	public boolean checkAndMarkVerticalExaggeration(Object key, DrawContext dc)
	{
		boolean isChanged = isVerticalExaggerationChanged(key, dc);
		if (!isChanged)
		{
			return false;
		}
		markVerticalExaggeration(key, dc);
		return true;
	}

}
