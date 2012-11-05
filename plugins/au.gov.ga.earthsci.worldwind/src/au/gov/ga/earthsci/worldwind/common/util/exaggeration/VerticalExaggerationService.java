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

/**
 * An interface for services that can apply vertical exaggeration to an elevation value
 * <p/>
 * Supports the detection of changed vertical exaggeration properties. Objects that wish to monitor changes should
 * first call {@link #markVerticalExaggeration(Object)}, then call {@link #isVerticalExaggerationChanged(Object)}.
 * A mark can be cleared using {@link #clearMark(Object)}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface VerticalExaggerationService
{

	/** 
	 * @return The exaggerated elevation value 
	 */
	double applyVerticalExaggeration(DrawContext dc, double elevation);
	
	/**
	 * @return The real-world elevation after vertical exaggeration has been removed 
	 */
	double unapplyVerticalExaggeration(DrawContext dc, double exaggeratedElevation);
	
	/** 
	 * @return The global vertical exaggeration value 
	 */
	double getGlobalVerticalExaggeration(DrawContext dc);
	
	/**
	 * @return The elevation at the given point, unexaggerated
	 */
	double getUnexaggeratedElevation(DrawContext dc, Angle latitude, Angle longitude);
	
	/**
	 * Mark the current vertical exaggeration settings against the provided object.
	 * <p/>
	 * Used to detect a change in the vertical exaggeration.
	 * <p/>
	 * Objects that wish to monitor changes should first call {@link #markVerticalExaggeration(Object)}, 
	 * then call {@link #isVerticalExaggerationChanged(Object)}.
	 */
	void markVerticalExaggeration(Object key, DrawContext dc);
	
	/**
	 * Clear the marked vertical exaggeration settings for the provided object
	 */
	void clearMark(Object key);
	
	/**
	 * Returns whether the vertical exaggeration settings have changed since object's last call to {@link #markVerticalExaggeration(Object)}.
	 * <p/>
	 * Objects that wish to monitor changes should first call {@link #markVerticalExaggeration(Object)}, 
	 * then call {@link #isVerticalExaggerationChanged(Object)}.
	 * 
	 * @return Whether the vertical exaggeration has changed since the last call to {@link #markVerticalExaggeration(Object)}.
	 */
	boolean isVerticalExaggerationChanged(Object key, DrawContext dc);
	
	/**
	 * Equivalent to a call to {@link #isVerticalExaggerationChanged(Object)} followed by {@link #markVerticalExaggeration(Object)}.
	 *
	 * @return Whether the vertical exaggeration has changed since the last call to {@link #markVerticalExaggeration(Object)}.
	 */
	boolean checkAndMarkVerticalExaggeration(Object key, DrawContext dc);
}
