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
package au.gov.ga.earthsci.core.tree;

/**
 * Callback passed to the {@link ILazyTreeNode#load(ILazyTreeNodeCallback)}
 * method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILazyTreeNodeCallback
{
	/**
	 * Called when the node loads its children. Can be called more than once.
	 * For example, if the node loads children from a cached resource, and then
	 * from a retrieved resource, this would be called twice.
	 */
	void loaded();
}
