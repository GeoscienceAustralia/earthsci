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
package au.gov.ga.earthsci.worldwind.common.layers.screenoverlay;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Parameter list keys for the {@link ScreenOverlayLayer} layer
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ScreenOverlayKeys extends AVKeyMore
{
	final static String OVERLAY_CONTENT = "au.gov.ga.worldwind.viewer.layers.screenoverlay.OverlayContent";
	
	final static String MIN_HEIGHT = "au.gov.ga.worldwind.viewer.layers.screenoverlay.MinHeight";
	final static String MAX_HEIGHT = "au.gov.ga.worldwind.viewer.layers.screenoverlay.MaxHeight";
	final static String MIN_WIDTH = "au.gov.ga.worldwind.viewer.layers.screenoverlay.MinWidth";
	final static String MAX_WIDTH = "au.gov.ga.worldwind.viewer.layers.screenoverlay.MaxWidth";
	
	final static String DRAW_BORDER = "au.gov.ga.worldwind.viewer.layers.screenoverlay.DrawBorder";
	final static String BORDER_WIDTH = "au.gov.ga.worldwind.viewer.layers.screenoverlay.BorderWidth";
	final static String BORDER_COLOR = "au.gov.ga.worldwind.viewer.layers.screenoverlay.BorderColor";
	
}
