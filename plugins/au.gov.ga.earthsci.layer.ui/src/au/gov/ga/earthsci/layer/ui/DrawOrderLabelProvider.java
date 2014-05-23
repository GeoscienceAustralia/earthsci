/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.layer.ui;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.common.ui.util.TextStyler;
import au.gov.ga.earthsci.common.ui.viewers.IFireableLabelProvider;
import au.gov.ga.earthsci.layer.DrawOrder;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;

/**
 * Label provider for the draw order viewer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DrawOrderLabelProvider extends ColumnLabelProvider implements IFireableLabelProvider, IStyledLabelProvider
{
	private final IconLoader iconLoader = new IconLoader(this);
	private final TextStyler structureStyler = new TextStyler();

	public DrawOrderLabelProvider()
	{
		structureStyler.style.foreground = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
	}

	@Override
	public String getText(Object element)
	{
		if (element instanceof DrawOrderModel.DrawOrderDrawOrderModelElement)
		{
			DrawOrderModel.DrawOrderDrawOrderModelElement drawOrderElement =
					(DrawOrderModel.DrawOrderDrawOrderModelElement) element;
			return DrawOrder.getLabel(drawOrderElement.drawOrder);
		}
		if (element instanceof DrawOrderModel.LayerDrawOrderModelElement)
		{
			DrawOrderModel.LayerDrawOrderModelElement layerElement =
					(DrawOrderModel.LayerDrawOrderModelElement) element;
			return layerElement.node.getLabelOrName();
		}
		return super.getText(element);
	}

	@Override
	public StyledString getStyledText(Object element)
	{
		StyledString string = new StyledString(getText(element));
		if (element instanceof DrawOrderModel.LayerDrawOrderModelElement)
		{
			DrawOrderModel.LayerDrawOrderModelElement layerElement =
					(DrawOrderModel.LayerDrawOrderModelElement) element;
			String structure = buildStructureString(layerElement.node.getParent());
			if (structure != null)
			{
				string.append(" "); //$NON-NLS-1$
				string.append("(" + structure + ")", structureStyler); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return string;
	}

	private String buildStructureString(ILayerTreeNode node)
	{
		if (node == null || node.isRoot())
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		while (!node.isRoot())
		{
			sb.insert(0, "/" + node.getLabelOrName()); //$NON-NLS-1$
			node = node.getParent();
		}
		return sb.substring(1);
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof DrawOrderModel.DrawOrderDrawOrderModelElement)
		{
			return ImageRegistry.getInstance().get(ImageRegistry.ICON_FOLDER);
		}
		if (element instanceof DrawOrderModel.LayerDrawOrderModelElement)
		{
			DrawOrderModel.LayerDrawOrderModelElement layerElement =
					(DrawOrderModel.LayerDrawOrderModelElement) element;
			return LayerTreeLabelProvider.getImage(layerElement.node, element, iconLoader);
		}
		return super.getImage(element);
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}
}
