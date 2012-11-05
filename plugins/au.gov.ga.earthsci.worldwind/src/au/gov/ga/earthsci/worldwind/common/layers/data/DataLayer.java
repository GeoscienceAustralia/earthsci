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
package au.gov.ga.earthsci.worldwind.common.layers.data;

import gov.nasa.worldwind.layers.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.earthsci.worldwind.common.layers.Bounded;
import au.gov.ga.earthsci.worldwind.common.util.Loader;
import au.gov.ga.earthsci.worldwind.common.util.Setupable;

/**
 * {@link Layer} which reads it's data from a single URL, using a
 * {@link DataProvider} subclass to download it's data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface DataLayer extends Layer, Loader, Setupable, Bounded
{
	/**
	 * @return The download url for this layer's data
	 * @throws MalformedURLException
	 */
	URL getUrl() throws MalformedURLException;

	/**
	 * @return The filename under which to store the downloaded data in the
	 *         cache
	 */
	String getDataCacheName();
}
