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
package au.gov.ga.earthsci.catalog.ui;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
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

import au.gov.ga.earthsci.application.IconLoader;
import au.gov.ga.earthsci.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.LoadingCatalogTreeNode;
import au.gov.ga.earthsci.common.ui.viewers.IControlProvider;
import au.gov.ga.earthsci.common.ui.viewers.IFireableLabelProvider;
import au.gov.ga.earthsci.common.ui.viewers.LoadingIconHelper;
import au.gov.ga.earthsci.common.util.ILabeled;
import au.gov.ga.earthsci.common.util.INamed;

/**
 * A {@link IControlProvider} for the catalog browser tree
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
public class CatalogTreeLabelProvider extends LabelProvider implements ILabelDecorator, IFireableLabelProvider,
		IStyledLabelProvider
{
	private final org.eclipse.jface.resource.ImageRegistry decoratedImageCache =
			new org.eclipse.jface.resource.ImageRegistry();

	private IconLoader iconLoader = new IconLoader(this);
	private LoadingIconHelper nodeLoader = new LoadingIconHelper(this);

	private boolean disposed = false;

	@PostConstruct
	public void postConstruct()
	{
		informationColor = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		FontData[] fontDatas = Display.getDefault().getSystemFont().getFontData();
		for (FontData fontData : fontDatas)
		{
			fontData.setStyle(SWT.BOLD);
			fontData.setHeight((int) (fontData.getHeight() * 0.8));
		}
		subscriptFont = new Font(Display.getDefault(), fontDatas);
	}

	@PreDestroy
	public void preDestroy()
	{
		subscriptFont.dispose();
	}

	@Override
	public Image getImage(final Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			return null;
		}

		if (element instanceof LoadingCatalogTreeNode)
		{
			return nodeLoader.getLoadingIcon(element);
		}

		ICatalogTreeNode node = (ICatalogTreeNode) element;
		URL url = node.getIconURL();
		return getImage(element, url);
	}

	@Override
	public String getText(Object element)
	{
		if (!(element instanceof ICatalogTreeNode))
		{
			if (element instanceof ILabeled)
			{
				return ((ILabeled) element).getLabelOrName();
			}
			else if (element instanceof INamed)
			{
				return ((INamed) element).getName();
			}
			return element.toString();
		}

		ICatalogTreeNode node = (ICatalogTreeNode) element;
		if (element instanceof LoadingCatalogTreeNode || element instanceof ErrorCatalogTreeNode)
		{
			return node.getName();
		}
		return node.getLabelOrName();
	}

	@Override
	public void dispose()
	{
		if (disposed)
		{
			return;
		}
		disposed = true;

		super.dispose();
		decoratedImageCache.dispose();
		iconLoader.dispose();

		//TODO we probably want to call dispose on the CatalogTreeNodeControlProviderRegistry at some point
		//but maybe not here because it feels wrong to dispose of a static factory's resources in a
		//non-static context (ie every time the catalog part is closed).
	}

	@Override
	public Image decorateImage(Image image, Object element)
	{
		return null;
	}

	@Override
	public String decorateText(String text, Object element)
	{
		return null;
	}

	private Image getImage(Object element, URL imageURL)
	{
		if (imageURL == null)
		{
			return null;
		}

		return iconLoader.getImage(element, imageURL);
	}

	@Override
	public StyledString getStyledText(Object element)
	{
		StyledString string = new StyledString(getText(element));
		if (element instanceof ICatalogTreeNode)
		{
			ICatalogTreeNode node = (ICatalogTreeNode) element;
			URL infoURL = node.getInformationURL();
			if (infoURL != null)
			{
				string.append("  i", informationStyler); //$NON-NLS-1$
			}
		}
		return string;
	}

	@Override
	public void fireLabelProviderChanged(LabelProviderChangedEvent event)
	{
		super.fireLabelProviderChanged(event);
	}

	private Color informationColor;
	private Font subscriptFont;
	private final Styler informationStyler = new Styler()
	{
		@Override
		public void applyStyles(TextStyle textStyle)
		{
			textStyle.foreground = informationColor;
			textStyle.font = subscriptFont;
		}
	};
}
