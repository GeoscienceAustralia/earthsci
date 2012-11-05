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
import gov.nasa.worldwind.layers.Layer;

import java.util.Collection;

import org.w3c.dom.Element;

/**
 * Kit that provides delegates for performing various tasks to the tiled
 * {@link Layer} s that support them. This interface also extends all of the
 * delegate interfaces; implementations should forward these delegate functions
 * to the actual delegate objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <TILE>
 *            Layer's tile type
 * @param <BOUNDS>
 *            Class that represents the bounds of each tile
 * @param <LEVEL>
 *            Class that represents a level of detail for the layer for which
 *            this {@link IDelegateKit} is for
 */
public interface IDelegateKit<TILE extends IDelegatorTile, BOUNDS, LEVEL> extends ITileRequesterDelegate<TILE>,
		ITileURLBuilderDelegate, IRetrieverFactoryDelegate, ITileFactoryDelegate<TILE, BOUNDS, LEVEL>,
		ITileReaderDelegate, IImageTransformerDelegate, IRenderDelegate
{
	/**
	 * Create a new {@link IDelegateKit} from an XML element.
	 * 
	 * @param domElement
	 *            XML element
	 * @return New {@link IDelegateKit}
	 */
	IDelegateKit<TILE, BOUNDS, LEVEL> createFromXML(Element domElement, AVList params);

	/**
	 * Save the provided {@link IDelegateKit} to XML.
	 * 
	 * @param kit
	 *            {@link IDelegateKit} to save
	 * @param context
	 *            XML element under which to add the elements.
	 * @return context
	 */
	Element saveToXML(Element context);

	/**
	 * @return A Collection of delegates for this kit
	 */
	Collection<IDelegate> getDelegates();

	/**
	 * Is the provided delegate string definition part of the default set of
	 * delegates for this DelegateKit?
	 */
	boolean isDefault(String definition);

	/**
	 * Set the {@link ITileFactoryDelegate}.
	 * 
	 * @param delegate
	 */
	void setTileFactoryDelegate(ITileFactoryDelegate<TILE, BOUNDS, LEVEL> delegate);

	/**
	 * Set the {@link ITileURLBuilderDelegate}.
	 * 
	 * @param delegate
	 */
	void setTileURLBuilderDelegate(ITileURLBuilderDelegate delegate);

	/**
	 * Set the {@link IRetrieverFactoryDelegate}.
	 * 
	 * @param delegate
	 */
	void setRetrieverFactoryDelegate(IRetrieverFactoryDelegate delegate);

	/**
	 * Set the {@link ITileRequesterDelegate}.
	 * 
	 * @param delegate
	 */
	void setTileRequesterDelegate(ITileRequesterDelegate<TILE> delegate);

	/**
	 * Add an {@link ITileReaderDelegate}.
	 * 
	 * @param delegate
	 */
	void addTileReaderDelegate(ITileReaderDelegate delegate);

	/**
	 * Add an {@link IRenderDelegate}.
	 * 
	 * @param delegate
	 */
	void addRenderDelegate(IRenderDelegate delegate);

	/**
	 * Add an {@link IImageTransformerDelegate}.
	 * 
	 * @param delegate
	 */
	void addImageTransformerDelegate(IImageTransformerDelegate delegate);
}
