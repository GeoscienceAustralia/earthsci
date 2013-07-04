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
package au.gov.ga.earthsci.discovery.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.common.ui.viewers.IFireableLabelProvider;
import au.gov.ga.earthsci.common.ui.viewers.LoadingIconHelper;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * {@link ILabelProvider} implementation that provides labels for the list of
 * discoveries for a single search.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryLabelProvider extends LabelProvider implements IFireableLabelProvider
{
	private final LoadingIconHelper loadingIconHelper = new LoadingIconHelper(this);

	@Override
	public String getText(Object element)
	{
		if (element instanceof IDiscovery)
		{
			IDiscovery discovery = (IDiscovery) element;
			IDiscoveryService service = discovery.getService();
			StringBuilder sb = new StringBuilder();
			String name = service.getName();
			if (Util.isEmpty(name) && service.getServiceURL() != null)
			{
				name = service.getServiceURL().toString();
			}
			sb.append(name);
			sb.append(" ("); //$NON-NLS-1$
			sb.append(discovery.getResultCount());
			sb.append(")"); //$NON-NLS-1$

			return sb.toString();
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof IDiscovery)
		{
			IDiscovery discovery = (IDiscovery) element;
			if (discovery.isLoading())
			{
				return loadingIconHelper.getLoadingIcon(element);
			}
			else if (discovery.getError() != null)
			{
				return ImageRegistry.getInstance().get(ImageRegistry.ICON_ERROR);
			}
		}
		return super.getImage(element);
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}
}
