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

import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

/**
 * Used to provide custom controls to a {@link IControlViewer} for items (ie
 * items in a table or tree).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IControlProvider
{
	/**
	 * Create a control used to display/edit the given element.
	 * 
	 * @param parent
	 *            The created control's parent
	 * @param element
	 *            Element being displayed/edited
	 * @param item
	 *            Item associated with the element
	 * @param editor
	 *            Editor that will be associated with the returned control
	 * @return New control used to display/edit the element
	 */
	Control getControl(Composite parent, Object element, Item item, ControlEditor editor);

	/**
	 * Update the given control to display/edit the given element. Called when
	 * the element is updated and the control need to be updated to reflect the
	 * element's changes. The control passed in is the same object that was
	 * returned by the {@link #getControl(Object, Item, ControlEditor)} method.
	 * <p/>
	 * Return true if the control should be reused. If false is returned from
	 * this method, the control is disposed, and a new one is created using
	 * {@link #getControl(Object, Item, ControlEditor)}.
	 * 
	 * @param control
	 *            Control used to display/edit the element
	 * @param element
	 *            Element being displayed/edited
	 * @param item
	 *            Item associated with the element
	 * @param editor
	 *            Editor that is associated with the control
	 * @return True to reuse the control, false to create a new one
	 */
	boolean updateControl(Control control, Object element, Item item, ControlEditor editor);

	/**
	 * Provides the ability to override the control's default bounds. Return
	 * null or the given bounds to use the default.
	 * 
	 * @param bounds
	 *            Default bounds to override
	 * @param control
	 *            Control used to display/edit the element
	 * @param element
	 *            Element being displayed/edited
	 * @param item
	 *            Item associated with the element
	 * @return Overridden bounds, or null to use the default
	 */
	Rectangle overrideBounds(Rectangle bounds, Control control, Object element, Item item);
}
