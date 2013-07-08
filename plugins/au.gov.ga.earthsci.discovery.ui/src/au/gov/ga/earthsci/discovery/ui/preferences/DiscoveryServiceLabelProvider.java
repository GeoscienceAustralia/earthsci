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
package au.gov.ga.earthsci.discovery.ui.preferences;

import java.net.URL;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.common.ui.viewers.IFireableLabelProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * Label provider for a table viewer displaying {@link IDiscoveryService}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryServiceLabelProvider extends LabelProvider implements ITableLabelProvider, IFireableLabelProvider
{
	private final IconLoader iconLoader = new IconLoader(this);

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if (!(element instanceof IDiscoveryService))
		{
			return element.toString();
		}
		IDiscoveryService service = (IDiscoveryService) element;
		return DiscoveryServiceViewerColumn.values()[columnIndex].getText(service);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		if (!(element instanceof IDiscoveryService) || columnIndex != 0)
		{
			return null;
		}
		IDiscoveryService service = (IDiscoveryService) element;
		URL iconURL = service.getProvider().getIconURL();
		if (iconURL != null)
		{
			return iconLoader.getImage(service, iconURL);
		}
		return null;
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}
}
