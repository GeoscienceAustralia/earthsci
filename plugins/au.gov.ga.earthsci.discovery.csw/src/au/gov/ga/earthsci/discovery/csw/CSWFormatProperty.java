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
package au.gov.ga.earthsci.discovery.csw;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.discovery.IDiscoveryService;
import au.gov.ga.earthsci.discovery.IDiscoveryServiceProperty;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * {@link IDiscoveryServiceProperty} for selecting the format of the CSW
 * service.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWFormatProperty implements IDiscoveryServiceProperty<CSWFormat>
{
	private final String XML_ATTRIBUTE = "value"; //$NON-NLS-1$

	@Override
	public String getId()
	{
		return "format"; //$NON-NLS-1$
	}

	@Override
	public String getLabel()
	{
		return "Format";
	}

	@Override
	public Control createControl(Composite parent, CSWFormat value, ModifyListener modifyListener)
	{
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		int selectedIndex = -1;
		for (int i = 0; i < CSWFormat.values().length; i++)
		{
			CSWFormat format = CSWFormat.values()[i];
			combo.add(format.label);
			if (value == format)
			{
				selectedIndex = i;
			}
		}
		if (selectedIndex >= 0)
		{
			combo.select(selectedIndex);
		}
		else
		{
			combo.select(Arrays.asList(CSWFormat.values()).indexOf(CSWFormat.GEONETWORK2));
		}
		combo.addModifyListener(modifyListener);
		return combo;
	}

	@Override
	public CSWFormat getValue(Control control)
	{
		int index = ((Combo) control).getSelectionIndex();
		if (index >= 0)
		{
			return CSWFormat.values()[index];
		}
		return null;
	}

	@Override
	public CSWFormat getValue(IDiscoveryService service)
	{
		return ((CSWDiscoveryService) service).getFormat();
	}

	@Override
	public boolean validate(Control control)
	{
		return ((Combo) control).getSelectionIndex() >= 0;
	}

	@Override
	public void persist(Element parent, CSWFormat value)
	{
		parent.setAttribute(XML_ATTRIBUTE, value.name());
	}

	@Override
	public CSWFormat unpersist(Element parent)
	{
		String attribute = parent.getAttribute(XML_ATTRIBUTE);
		if (Util.isBlank(attribute))
		{
			return null;
		}
		return CSWFormat.valueOf(attribute);
	}
}
