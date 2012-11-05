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
package au.gov.ga.earthsci.worldwind.common.layers.kml.relativeio;

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;

/**
 * Defines some extra properties for {@link KMLDoc}s that allow for better
 * resolving of relative document paths.
 * 
 * (Preferably the ideas in this package will be pushed into the WWJ SDK
 * someday).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface RelativeKMLDoc extends KMLDoc
{
	/**
	 * @return the original HREF element for this {@link KMLDoc} in the KML.
	 */
	String getHref();

	/**
	 * @return the parent {@link KMLDoc} from which this doc was referenced
	 *         (null if this is the root {@link KMLDoc}).
	 */
	KMLDoc getParent();

	/**
	 * KMZ files should be treated as containers (directories) when resolving
	 * relative paths. For instance, when resolving a parent directory with
	 * '..', the KMZ file should act as a directory.
	 * 
	 * @return whether this {@link KMLDoc} should act as a container (if it's a
	 *         KMZ file)
	 */
	boolean isContainer();
}
