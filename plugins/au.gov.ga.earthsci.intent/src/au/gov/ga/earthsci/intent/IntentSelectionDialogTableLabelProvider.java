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
package au.gov.ga.earthsci.intent;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.common.util.Util;

/**
 * Label provider for the table viewer in the {@link IntentSelectionDialog}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentSelectionDialogTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider
{
	private final ImageRegistry registry;

	public IntentSelectionDialogTableLabelProvider()
	{
		registry = new ImageRegistry(Display.getDefault());
	}

	@Override
	public void dispose()
	{
		registry.dispose();
		super.dispose();
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		IntentFilter filter = (IntentFilter) element;
		URL url = filter.getIcon();
		if (url != null)
		{
			Image image = registry.get(url.toString());
			if (image == null)
			{
				registry.put(url.toString(), ImageDescriptor.createFromURL(url));
				image = registry.get(url.toString());
			}
			return image;
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		IntentFilter filter = (IntentFilter) element;
		String label = filter.getLabel();
		if (!Util.isEmpty(label))
		{
			return label;
		}
		return filter.getHandler().getName();
	}
}
