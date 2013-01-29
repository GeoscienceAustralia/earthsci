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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.earthsci.worldwind.common.IWorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointLayerHelper;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndText;
import au.gov.ga.earthsci.worldwind.common.util.DefaultLauncher;

/**
 * {@link PointLayer} implementation which extends {@link IconLayer} and uses
 * Icons to represent points.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IconPointLayer extends IconLayer implements PointLayer, SelectListener
{
	private final PointLayerHelper helper;
	private WWIcon pickedIcon;

	public IconPointLayer(PointLayerHelper helper)
	{
		this.helper = helper;
		IWorldWindowRegistry.INSTANCE.addSelectListener(this);
	}
	
	@Override
	public void render(DrawContext dc)
	{
		if (isEnabled())
		{
			helper.requestPoints(this);
		}
		super.render(dc);
	}

	@Override
	public Sector getSector()
	{
		return helper.getSector();
	}

	@Override
	public void addPoint(Position position, AVList attributeValues)
	{
		StyleAndText properties = helper.getStyle(attributeValues);
		UserFacingIcon icon = new UserFacingIcon();
		icon.setPosition(position);
		icon.setToolTipText(properties.text);
		icon.setValue(AVKey.URL, properties.link);
		properties.style.setPropertiesFromAttributes(helper.getContext(), attributeValues, icon);
		this.addIcon(icon);
	}

	@Override
	public void loadComplete()
	{
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return helper.getUrl();
	}

	@Override
	public String getDataCacheName()
	{
		return helper.getDataCacheName();
	}

	@Override
	public void selected(SelectEvent e)
	{
		if (e == null)
			return;

		PickedObject topPickedObject = e.getTopPickedObject();
		if (topPickedObject != null && topPickedObject.getObject() instanceof WWIcon)
		{
			if (pickedIcon != null)
			{
				highlight(pickedIcon, false);
			}

			pickedIcon = (WWIcon) topPickedObject.getObject();
			highlight(pickedIcon, true);

			if (e.getEventAction() == SelectEvent.LEFT_CLICK)
			{
				String link = pickedIcon.getStringValue(AVKey.URL);
				if (link != null)
				{
					try
					{
						URL url = new URL(link);
						DefaultLauncher.openURL(url);
					}
					catch (MalformedURLException m)
					{
					}
				}
			}
		}
		else if (pickedIcon != null)
		{
			highlight(pickedIcon, false);
			pickedIcon = null;
		}
	}

	protected void highlight(WWIcon icon, boolean highlight)
	{
		icon.setShowToolTip(highlight);
		icon.setHighlighted(highlight);
	}

	@Override
	public boolean isLoading()
	{
		return helper.getPointProvider().isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		helper.getPointProvider().addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		helper.getPointProvider().removeLoadingListener(listener);
	}
}
