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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;

/**
 * A helper for classes implementing the {@link IControlViewer} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IControlViewerHelper
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
	 * Associate the given user object with the viewer item.
	 * <p/>
	 * Should only be called by a {@link StructuredViewer}'s overridden
	 * associate() function.
	 * 
	 * @param element
	 * @param item
	 */
	void associate(Object element, Item item);

	/**
	 * Disassociate the item from the user object associated in the
	 * {@link #associate(Object, Item)} function.
	 * <p/>
	 * Should only be called by a {@link StructuredViewer}'s overriden
	 * disassociate() function.
	 * 
	 * @param item
	 */
	void disassociate(Item item);

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
