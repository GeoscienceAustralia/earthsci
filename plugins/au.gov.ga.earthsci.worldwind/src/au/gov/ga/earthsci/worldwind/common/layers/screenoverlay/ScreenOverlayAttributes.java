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

import java.awt.Color;
import java.net.URL;

/**
 * The 'model' for a {@link ScreenOverlayLayer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ScreenOverlayAttributes
{
	
	// Source data
	
	/** @return the source data URL, if one has been provided. If <code>null</code>, use the {@link #getSourceHtml()} method. */
	URL getSourceUrl();
	
	/** @return the source html content. */
	String getSourceHtml();
	
	/** @return a unique ID that can identify this overlay */
	String getSourceId();
	
	/** @return whether the source is of html format */
	boolean isSourceHtml();
	
	/** @return whether the source is an image file */
	boolean isSourceImage();
	
	// Positioning
	
	/** @return The position the overlay is to be placed on the screen */
	ScreenOverlayPosition getPosition();
	
	// Sizing
	
	/** @return the expression for the minimum height of the overlay */
	LengthExpression getMinHeight();
	
	/** @return the expression for the maximum height of the overlay */
	LengthExpression getMaxHeight();
	
	/** @return the height (in pixels) the overlay should occupy given the screen height. Min height is given priority. */
	float getHeight(float screenHeight);
	
	/** @return the expression for the minimum width of the overlay */
	LengthExpression getMinWidth();
	
	/** @return the expression for the maximum width of the overlay */
	LengthExpression getMaxWidth();
	
	/** @return the width (in pixels) the overlay should occupy given the screen width. Min width is given priority. */
	float getWidth(float screenWidth);
	
	// Styling
	
	/** @return whether or not to draw the border */
	boolean isDrawBorder();
	
	/** @return the border color, or <code>null</code> if no border is to be drawn */
	Color getBorderColor();
	
	/** @return the border width to use. If no border is to be drawn, will return 0. */
	int getBorderWidth();
	
}
