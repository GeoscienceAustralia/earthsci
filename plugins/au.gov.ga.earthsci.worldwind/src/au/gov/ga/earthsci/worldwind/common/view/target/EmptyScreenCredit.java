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
package au.gov.ga.earthsci.worldwind.common.view.target;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenCredit;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Empty {@link ScreenCredit} implementation that does nothing, useful for
 * subclassing.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EmptyScreenCredit implements ScreenCredit
{
	public EmptyScreenCredit()
	{
	}

	@Override
	public void render(DrawContext dc)
	{
	}

	@Override
	public void setViewport(Rectangle viewport)
	{
	}

	@Override
	public Rectangle getViewport()
	{
		return null;
	}

	@Override
	public void setOpacity(double opacity)
	{
	}

	@Override
	public double getOpacity()
	{
		return 1;
	}

	@Override
	public void setLink(String link)
	{
	}

	@Override
	public String getLink()
	{
		return null;
	}

	@Override
	public void pick(DrawContext dc, Point pickPoint)
	{
	}
}
