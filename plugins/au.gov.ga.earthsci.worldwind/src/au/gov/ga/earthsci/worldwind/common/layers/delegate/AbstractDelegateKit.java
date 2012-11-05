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
package au.gov.ga.earthsci.worldwind.common.layers.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

/**
 * Abstract generic implementation of the {@link IDelegateKit} interface. Stores
 * the delegate objects for each of the delegate types, and forward the delegate
 * interface functions to the stored delegate implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractDelegateKit<TILE extends IDelegatorTile, BOUNDS, LEVEL> implements
		IDelegateKit<TILE, BOUNDS, LEVEL>
{
	//default delegate implementations
	protected ITileRequesterDelegate<TILE> requesterDelegate;
	protected ITileURLBuilderDelegate tileURLBuilderDelegate;
	protected IRetrieverFactoryDelegate retrieverDelegate;
	protected ITileFactoryDelegate<TILE, BOUNDS, LEVEL> factoryDelegate;
	protected final List<ITileReaderDelegate> readerDelegates = new ArrayList<ITileReaderDelegate>();
	protected final List<IImageTransformerDelegate> transformerDelegates = new ArrayList<IImageTransformerDelegate>();
	protected final List<IRenderDelegate> renderDelegates = new ArrayList<IRenderDelegate>();

	/**
	 * @return New instance of this {@link IDelegateKit}.
	 */
	protected abstract AbstractDelegateKit<TILE, BOUNDS, LEVEL> createNewInstance();

	/**
	 * @return {@link IDelegateFactory} for this {@link IDelegateKit}.
	 */
	protected abstract IDelegateFactory getFactory();

	@Override
	public Collection<IDelegate> getDelegates()
	{
		Set<IDelegate> delegates = new HashSet<IDelegate>();
		delegates.add(requesterDelegate);
		delegates.add(retrieverDelegate);
		delegates.add(factoryDelegate);
		delegates.addAll(readerDelegates);
		delegates.addAll(transformerDelegates);
		delegates.addAll(renderDelegates);
		return delegates;
	}

	@Override
	public IDelegateKit<TILE, BOUNDS, LEVEL> createFromXML(Element domElement, AVList params)
	{
		AbstractDelegateKit<TILE, BOUNDS, LEVEL> kit = createNewInstance();
		IDelegateFactory factory = getFactory();

		XPath xpath = WWXML.makeXPath();
		Element delegatesElement = WWXML.getElement(domElement, "Delegates", xpath);
		if (delegatesElement != null)
		{
			Element[] elements = WWXML.getElements(delegatesElement, "Delegate", xpath);
			if (elements != null)
			{
				for (Element element : elements)
				{
					String definition = element.getTextContent();
					if (definition != null && definition.length() > 0)
					{
						IDelegate delegate = factory.createDelegate(definition, domElement, params);
						if (!kit.trySetOrAddDelegate(delegate))
						{
							throw new IllegalArgumentException("Unrecognized delegate: " + delegate);
						}
					}
				}
			}
		}
		return kit;
	}

	/**
	 * Attempt to set or add a delegate; return false if this delegate is not
	 * recognized and therefore not set or added.
	 */
	protected boolean trySetOrAddDelegate(IDelegate delegate)
	{
		if (delegate instanceof IRetrieverFactoryDelegate)
		{
			setRetrieverFactoryDelegate((IRetrieverFactoryDelegate) delegate);
			return true;
		}
		if (delegate instanceof ITileURLBuilderDelegate)
		{
			setTileURLBuilderDelegate((ITileURLBuilderDelegate) delegate);
			return true;
		}
		if (delegate instanceof ITileReaderDelegate)
		{
			addTileReaderDelegate((ITileReaderDelegate) delegate);
			return true;
		}
		if (delegate instanceof IImageTransformerDelegate)
		{
			addImageTransformerDelegate((IImageTransformerDelegate) delegate);
			return true;
		}
		if (delegate instanceof IRenderDelegate)
		{
			addRenderDelegate((IRenderDelegate) delegate);
			return true;
		}
		return false;
	}


	@Override
	public Element saveToXML(Element context)
	{
		Collection<IDelegate> delegates = getDelegates();
		Element delegatesElement = WWXML.appendElement(context, "Delegates");
		for (IDelegate delegate : delegates)
		{
			String definition = delegate.toDefinition(context);
			//only append the XML element if the delegate is not one of the defaults
			if (!isDefault(definition))
			{
				WWXML.appendText(delegatesElement, "Delegate", definition);
			}
		}
		return context;
	}

	/* ********************
	 * Setters and adders *
	 ******************** */

	@Override
	public void setTileFactoryDelegate(ITileFactoryDelegate<TILE, BOUNDS, LEVEL> delegate)
	{
		this.factoryDelegate = delegate;
	}

	@Override
	public void setTileURLBuilderDelegate(ITileURLBuilderDelegate delegate)
	{
		this.tileURLBuilderDelegate = delegate;
	}

	@Override
	public void setRetrieverFactoryDelegate(IRetrieverFactoryDelegate delegate)
	{
		this.retrieverDelegate = delegate;
	}

	@Override
	public void setTileRequesterDelegate(ITileRequesterDelegate<TILE> delegate)
	{
		this.requesterDelegate = delegate;
	}

	@Override
	public void addTileReaderDelegate(ITileReaderDelegate delegate)
	{
		readerDelegates.add(delegate);
	}

	@Override
	public void addRenderDelegate(IRenderDelegate delegate)
	{
		renderDelegates.add(delegate);
	}

	@Override
	public void addImageTransformerDelegate(IImageTransformerDelegate delegate)
	{
		transformerDelegates.add(delegate);
	}

	/* ******************************
	 * Delegate Interface functions *
	 ****************************** */

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		for (IImageTransformerDelegate transformer : transformerDelegates)
		{
			image = transformer.transformImage(image, tile);
		}
		return image;
	}

	@Override
	public <T extends IDelegatorTile> BufferedImage readImage(T tile, URL url, Globe globe) throws IOException
	{
		for (ITileReaderDelegate reader : readerDelegates)
		{
			BufferedImage image = reader.readImage(tile, url, globe);
			if (image != null)
				return image;
		}
		return null;
	}

	@Override
	public TILE createTextureTile(BOUNDS bounds, LEVEL level, int row, int col)
	{
		return factoryDelegate.createTextureTile(bounds, level, row, col);
	}

	@Override
	public TileKey transformTileKey(TileKey tileKey)
	{
		return factoryDelegate.transformTileKey(tileKey);
	}

	@Override
	public Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		return retrieverDelegate.createRetriever(url, postProcessor);
	}

	@Override
	public void forceTextureLoad(TILE tile, IDelegatorLayer<TILE> layer)
	{
		requesterDelegate.forceTextureLoad(tile, layer);
	}

	@Override
	public URL getLocalTileURL(TILE tile, IDelegatorLayer<TILE> layer, boolean searchClassPath)
	{
		return requesterDelegate.getLocalTileURL(tile, layer, searchClassPath);
	}

	@Override
	public Runnable createRequestTask(TILE tile, IDelegatorLayer<TILE> layer)
	{
		return requesterDelegate.createRequestTask(tile, layer);
	}

	@Override
	public URL getRemoteTileURL(IDelegatorTile tile, String imageFormat) throws java.net.MalformedURLException
	{
		if (tileURLBuilderDelegate != null)
			return tileURLBuilderDelegate.getRemoteTileURL(tile, imageFormat);
		return tile.getResourceURL(imageFormat);
	}

	@Override
	public void preRender(DrawContext dc)
	{
		for (IRenderDelegate renderDelegate : renderDelegates)
		{
			renderDelegate.preRender(dc);
		}
	}

	@Override
	public void postRender(DrawContext dc)
	{
		for (IRenderDelegate renderDelegate : renderDelegates)
		{
			renderDelegate.postRender(dc);
		}
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return null;
	}
}
