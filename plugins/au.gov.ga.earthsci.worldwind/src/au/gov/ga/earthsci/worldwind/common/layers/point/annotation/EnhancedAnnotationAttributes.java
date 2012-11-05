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
package au.gov.ga.earthsci.worldwind.common.layers.point.annotation;

import gov.nasa.worldwind.render.AnnotationAttributes;

/**
 * Enhanced annotation attributes that contains additional attributes for use
 * with the {@link EnhancedAnnotation} class.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EnhancedAnnotationAttributes extends AnnotationAttributes
{
	private static final Double DEFAULT_FADE_DISTANCE = 4E4;

	/**
	 * Create a new instance initialised with the values from the provided
	 * {@link AnnotationAttributes}.
	 */
	public EnhancedAnnotationAttributes(AnnotationAttributes attributes)
	{
		super.setDefaults(attributes);
	}

	/**
	 * Create a new instance initialised default values
	 */
	public EnhancedAnnotationAttributes()
	{
	}

	// Thresholds to control the annotation visibility based on distance from the camera eye to the marker
	private Double minEyeDistance = null;
	private Double maxEyeDistance = null;

	// The distance it takes to fade from 0% to 100% opacity
	private Double fadeDistance = DEFAULT_FADE_DISTANCE;

	public Double getMinEyeDistance()
	{
		return minEyeDistance;
	}

	public void setMinEyeDistance(Double minEyeDistance)
	{
		this.minEyeDistance = minEyeDistance;
	}

	public Double getMaxEyeDistance()
	{
		return maxEyeDistance;
	}

	public void setMaxEyeDistance(Double maxEyeDistance)
	{
		this.maxEyeDistance = maxEyeDistance;
	}

	public Double getFadeDistance()
	{
		return fadeDistance == null ? 1d : fadeDistance;
	}

	public void setFadeDistance(Double fadeDistance)
	{
		this.fadeDistance = fadeDistance;
	}
}
