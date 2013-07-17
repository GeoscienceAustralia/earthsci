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
package au.gov.ga.earthsci.discovery;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;

/**
 * Property for an {@link IDiscoveryService}. {@link IDiscoveryProvider}s can
 * define a number of properties that must be defined for the creation of their
 * services.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscoveryServiceProperty<V>
{
	/**
	 * @return Unique id of this property
	 */
	String getId();

	/**
	 * @return Label to display for this property; used in the UI
	 */
	String getLabel();

	/**
	 * Create a control that is used to choose this property's value.
	 * 
	 * @param parent
	 *            Parent of the control
	 * @param value
	 *            Initial value to set the control to, could be
	 *            <code>null</code>
	 * @param modifyListener
	 *            Listener that should be added to the control that is called
	 *            when the control's value is modified
	 * @return Control that was created
	 */
	Control createControl(Composite parent, V value, ModifyListener modifyListener);

	/**
	 * Get the value for this property from the control. The provided control is
	 * the same control created by the
	 * {@link #createControl(Composite, Object, ModifyListener)} method.
	 * 
	 * @param control
	 *            Control to get the property value from
	 * @return Property value defined by the control's value
	 */
	V getValue(Control control);

	/**
	 * Get the value for this property from the given service.
	 * 
	 * @param service
	 *            Service to get the value for this property from
	 * @return Property value defined by the given service
	 */
	V getValue(IDiscoveryService service);

	/**
	 * Validate that the given control contains a valid value for this property.
	 * The provided control is the same control created by the
	 * {@link #createControl(Composite, Object, ModifyListener)} method.
	 * 
	 * @param control
	 *            Control to validate
	 * @return True if the control contains a valid value
	 */
	boolean validate(Control control);

	/**
	 * Save the given value for this property to XML, in the given element.
	 * 
	 * @param parent
	 *            XML element to save the value within
	 * @param value
	 *            Value to save
	 */
	void persist(Element parent, V value);

	/**
	 * Load a value for this property from the given XML element.
	 * 
	 * @param parent
	 *            XML element to load the value from
	 * @return Loaded value
	 */
	V unpersist(Element parent);
}
