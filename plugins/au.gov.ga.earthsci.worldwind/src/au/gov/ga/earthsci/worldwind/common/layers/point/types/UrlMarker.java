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
package au.gov.ga.earthsci.worldwind.common.layers.point.types;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

/**
 * Extension to the {@link BasicMarker} class which adds a 'url' property, so
 * that markers can have associated urls.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class UrlMarker extends BasicMarker
{
	private String url;
	private String tooltipText;
	private Material backupMaterial;

	public UrlMarker(Position position, MarkerAttributes attrs)
	{
		super(position, attrs);
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getTooltipText()
	{
		return tooltipText;
	}

	public void setTooltipText(String tooltipText)
	{
		this.tooltipText = tooltipText;
	}
	
	public void backupMaterial()
	{
		if(backupMaterial == null)
			backupMaterial = getAttributes().getMaterial();
	}
	
	public void restoreMaterial()
	{
		if(backupMaterial != null)
			getAttributes().setMaterial(backupMaterial);
	}
}
