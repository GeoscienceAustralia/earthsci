/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.bookmark.properties.layer;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import au.gov.ga.earthsci.bookmark.Messages;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * A property that records the layer state including enabled layers, opacity.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayersProperty implements IBookmarkProperty
{

	public static final String TYPE = "au.gov.ga.earthsci.bookmark.properties.layers"; //$NON-NLS-1$

	/**
	 * Map of {@code Layer URI -> opacity}. Inclusion in the map implies
	 * enabled.
	 */
	private Map<URI, Double> layerState = new ConcurrentHashMap<URI, Double>();

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getName()
	{
		return Messages.LayersProperty_Name;
	}

	/**
	 * Return the (unmodifiable) layer state recorded on this property.
	 * <p/>
	 * To add additional layer state, use {@link #addLayer}.
	 */
	public Map<URI, Double> getLayerState()
	{
		return Collections.unmodifiableMap(layerState);
	}

	/**
	 * Set the layer state on this property, replacing any already stored.
	 */
	public void setLayerState(Map<URI, Double> newLayerState)
	{
		layerState.clear();
		layerState.putAll(newLayerState);
	}

	/**
	 * Add additional layer state to this property.
	 * 
	 * @param uri
	 *            The URI of the layer
	 * @param opacity
	 *            The opacity of the layer
	 */
	public void addLayer(URI uri, Double opacity)
	{
		layerState.put(uri, opacity);
	}

}
