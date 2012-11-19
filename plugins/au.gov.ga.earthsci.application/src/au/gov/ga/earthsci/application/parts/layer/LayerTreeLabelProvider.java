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
package au.gov.ga.earthsci.application.parts.layer;

import java.net.URL;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.application.IFireableLabelProvider;
import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;

/**
 * Label provider for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeLabelProvider extends ObservableMapLabelProvider implements ILabelDecorator,
		IStyledLabelProvider, IFireableLabelProvider
{
	private final IconLoader iconLoader = new IconLoader(this);

	private boolean disposed = false;

	private final Color informationColor;
	private final Color legendColor;
	private final Font subscriptFont;
	private final Styler informationStyler = new Styler()
	{
		@Override
		public void applyStyles(TextStyle textStyle)
		{
			textStyle.foreground = informationColor;
			textStyle.font = subscriptFont;
		}
	};
	private final Styler legendStyler = new Styler()
	{
		@Override
		public void applyStyles(TextStyle textStyle)
		{
			textStyle.foreground = legendColor;
			textStyle.font = subscriptFont;
		}
	};

	public LayerTreeLabelProvider(IObservableMap[] attributeMaps)
	{
		super(attributeMaps);
		informationColor = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		legendColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
		FontData[] fontDatas = Display.getDefault().getSystemFont().getFontData();
		for (FontData fontData : fontDatas)
		{
			fontData.setStyle(SWT.BOLD);
			fontData.setHeight((int) (fontData.getHeight() * 0.8));
		}
		subscriptFont = new Font(Display.getDefault(), fontDatas);
	}

	@Override
	public void dispose()
	{
		//because this object is acting as both the decorator and the provider,
		//dispose is called twice, causing a NPE in the super class
		//workaround: set a flag when disposed, disabling multiple disposals
		if (disposed)
			return;
		disposed = true;

		super.dispose();
		iconLoader.dispose();
		subscriptFont.dispose();
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if (element instanceof LayerNode)
		{
			LayerNode layer = (LayerNode) element;
			String label = layer.getLabelOrName();
			if (layer.getOpacity() < 1)
			{
				label += String.format(" (%d%%)", (int) (layer.getOpacity() * 100)); //$NON-NLS-1$
			}
			return label;
		}
		else if (element instanceof FolderNode)
		{
			FolderNode folder = (FolderNode) element;
			return folder.getName();
		}
		return super.getColumnText(element, columnIndex);
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode layer = (ILayerTreeNode) element;
			URL imageURL = layer.getIconURL();
			if (imageURL != null)
			{
				return iconLoader.getImage(element, imageURL);
			}
		}
		return super.getImage(element);
	}

	@Override
	public Image decorateImage(Image image, Object element)
	{
		return null;
	}

	@Override
	public String decorateText(String text, Object element)
	{
		return text;
	}

	@Override
	public StyledString getStyledText(Object element)
	{
		StyledString string = new StyledString(getColumnText(element, 0));
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode layerNode = (ILayerTreeNode) element;
			if (layerNode.getInfoURL() != null || layerNode.getLegendURL() != null)
			{
				string.append(" "); //$NON-NLS-1$
				if (layerNode.getInfoURL() != null)
				{
					string.append(" i", informationStyler); //$NON-NLS-1$
				}
				if (layerNode.getLegendURL() != null)
				{
					string.append(" L", legendStyler); //$NON-NLS-1$
				}
			}
		}
		return string;
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}
}
