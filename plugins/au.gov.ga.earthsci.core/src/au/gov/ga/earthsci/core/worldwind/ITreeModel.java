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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.Model;
import au.gov.ga.earthsci.core.model.layer.FolderNode;

/**
 * {@link Model} subinterface that supports a layer list in a tree hierarchy.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITreeModel extends Model
{
	/**
	 * @return The root node of the layer list tree.
	 */
	FolderNode getRootNode();
}
