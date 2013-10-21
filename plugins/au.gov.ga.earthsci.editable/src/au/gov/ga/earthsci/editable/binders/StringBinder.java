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
package au.gov.ga.earthsci.editable.binders;

import org.eclipse.sapphire.modeling.ValueProperty;

import au.gov.ga.earthsci.editable.IValueBinder;

/**
 * {@link IValueBinder} implementation for string objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StringBinder implements IValueBinder<String>
{
	@Override
	public String get(String object, ValueProperty property)
	{
		return object;
	}

	@Override
	public void set(String value, String object, ValueProperty property)
	{
		throw new UnsupportedOperationException("Strings are immutable, cannot set value"); //$NON-NLS-1$
	}
}