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
package au.gov.ga.earthsci.worldwind.common.layers.mouse;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.IconRenderer;
import gov.nasa.worldwind.render.WWIcon;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Layer that renders an icon at the point on the globe's surface where the
 * mouse should be. This is useful when rendering with a stereo view, the
 * operating system mouse cursor can destroy the illusion of depth.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MouseLayer extends AbstractLayer implements PositionListener
{
	private final IconRenderer iconRenderer = new IconRenderer();
	private final Component wwd;
	private final WWIcon icon;
	private final List<WWIcon> icons = new ArrayList<WWIcon>();
	private final Cursor blankCursor;
	private boolean mouseReplaced = false;

	public MouseLayer(WorldWindow wwd, WWIcon icon)
	{
		if (!(wwd instanceof Component))
		{
			throw new IllegalArgumentException("WorldWindow must be a subclass of component");
		}
		wwd.addPositionListener(this);
		this.wwd = (Component) wwd;
		this.iconRenderer.setPedestal(null);
		this.icon = icon;
		icons.add(icon);

		setPickEnabled(false);

		Toolkit tk = Toolkit.getDefaultToolkit();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		blankCursor = tk.createCustomCursor(image, new Point(0, 0), "BlackCursor");
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		this.iconRenderer.render(dc, icons);
	}

	@Override
	protected void doPick(DrawContext dc, Point pickPoint)
	{
		//don't pick
	}

	@Override
	public void moved(PositionEvent event)
	{
		if (isEnabled())
		{
			Position position = event.getPosition();
			if (position == null)
			{
				icon.setVisible(false);
				restoreMouse();
			}
			else
			{
				icon.setVisible(true);
				icon.setPosition(position);
				replaceMouse();
			}
		}
	}

	private void replaceMouse()
	{
		wwd.setCursor(blankCursor);
		mouseReplaced = true;
	}

	private void restoreMouse()
	{
		wwd.setCursor(null);
		mouseReplaced = false;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if (!enabled && mouseReplaced)
		{
			restoreMouse();
		}
	}
}
