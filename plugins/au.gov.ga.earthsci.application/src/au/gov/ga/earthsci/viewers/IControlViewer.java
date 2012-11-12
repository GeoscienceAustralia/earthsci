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
package au.gov.ga.earthsci.viewers;

import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;

/**
 * A viewer that provides the ability to display custom controls as viewer
 * items, provided by a {@link IControlProvider}. Implementations must be
 * subclasses of {@link StructuredViewer} (or its subclasses).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IControlViewer extends IInputSelectionProvider, IPostSelectionProvider
{
	/**
	 * @return The {@link IControlProvider} associated with this viewer.
	 */
	IControlProvider getControlProvider();

	/**
	 * Set the {@link IControlProvider} used by this viewer to create custom
	 * controls for items.
	 * 
	 * @param controlProvider
	 */
	void setControlProvider(IControlProvider controlProvider);

	/**
	 * Return the control visible for the given item. Note that this is not the
	 * control created by the {@link IControlProvider}; rather its parent
	 * {@link Composite}.
	 * 
	 * @param item
	 *            Viewer item to get the control for
	 * @return Control associated with the given item
	 */
	Composite getControlForItem(Item item);
}
