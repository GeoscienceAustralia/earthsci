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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;

import java.util.Collection;
import java.util.HashSet;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.AbstractDelegateKit;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegateFactory;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegateKit;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRetrieverFactoryDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.retriever.HttpRetrieverFactoryDelegate;

/**
 * Default {@link IDelegateKit} used by delegator tiled image layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImageDelegateKit extends AbstractDelegateKit<DelegatorTextureTile, Sector, Level>
{
	private final static Collection<String> defaultDelegateDefinitions = new HashSet<String>();

	static
	{
		Collection<IDelegate> defaultDelegates = new ImageDelegateKit().getDelegates();
		for (IDelegate delegate : defaultDelegates)
		{
			defaultDelegateDefinitions.add(delegate.toDefinition(null));
		}
	}

	public ImageDelegateKit()
	{
		requesterDelegate =
				(ImageURLRequesterDelegate) getFactory().createDelegate(
						new ImageURLRequesterDelegate().toDefinition(null), null, null);
		retrieverDelegate =
				(IRetrieverFactoryDelegate) getFactory().createDelegate(
						new HttpRetrieverFactoryDelegate().toDefinition(null), null, null);
		factoryDelegate =
				(TextureTileFactoryDelegate) getFactory().createDelegate(
						new TextureTileFactoryDelegate().toDefinition(null), null, null);
	}

	@Override
	public ImageDelegateKit createFromXML(Element domElement, AVList params)
	{
		return (ImageDelegateKit) super.createFromXML(domElement, params);
	}

	@Override
	protected AbstractDelegateKit<DelegatorTextureTile, Sector, Level> createNewInstance()
	{
		return new ImageDelegateKit();
	}

	@Override
	public boolean isDefault(String definition)
	{
		return defaultDelegateDefinitions.contains(definition);
	}

	@Override
	protected boolean trySetOrAddDelegate(IDelegate delegate)
	{
		if (super.trySetOrAddDelegate(delegate))
		{
			return true;
		}
		if (delegate instanceof IImageTileRequesterDelegate)
		{
			setTileRequesterDelegate((IImageTileRequesterDelegate) delegate);
			return true;
		}
		if (delegate instanceof IImageTileFactoryDelegate)
		{
			setTileFactoryDelegate((IImageTileFactoryDelegate) delegate);
			return true;
		}
		return false;
	}

	@Override
	protected IDelegateFactory getFactory()
	{
		return ImageDelegateFactory.get();
	}
}
