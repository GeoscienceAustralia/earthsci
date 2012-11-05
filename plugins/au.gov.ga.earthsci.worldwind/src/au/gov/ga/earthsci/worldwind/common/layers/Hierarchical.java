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
package au.gov.ga.earthsci.worldwind.common.layers;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.tree.TreeNode;

import java.util.ArrayList;

/**
 * This interface indicates that this layer contains a hierarchy of enableable
 * children. An example of this a KMLLayer which has children such as network
 * links or placemarks.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Hierarchical extends Layer
{
	void addHierarchicalListener(HierarchicalListener listener);

	void removeHierarchicalListener(HierarchicalListener listener);

	public static interface HierarchicalListener
	{
		void hierarchyChanged(Layer layer, TreeNode node);
	}

	/**
	 * Helper list containing a collection of {@link HierarchicalListener}s, for
	 * easy notification.
	 */
	public static class HierarchicalListenerList extends ArrayList<HierarchicalListener>
	{
		public void notifyListeners(Layer layer, TreeNode node)
		{
			for (int i = size() - 1; i >= 0; i--)
				get(i).hierarchyChanged(layer, node);
		}
	}
}
