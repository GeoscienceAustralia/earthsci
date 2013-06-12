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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;

import au.gov.ga.earthsci.common.ui.util.UIUtil;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;

/**
 * {@link ILabelProvider} implementation for the list of discovery results for a
 * given discovery.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryResultLabelProvider extends OwnerDrawLabelProvider
{
	private final IDiscoveryResultLabelProvider discoveryLabelProvider;

	private final Font normalFont;
	private final Font boldFont;

	private ColumnViewer viewer;

	public DiscoveryResultLabelProvider(IDiscoveryResultLabelProvider discoveryLabelProvider)
	{
		this.discoveryLabelProvider = discoveryLabelProvider;

		Display display = Display.getDefault();
		normalFont = display.getSystemFont();
		FontData[] fontDatas = normalFont.getFontData();
		for (FontData fontData : fontDatas)
		{
			fontData.setStyle(SWT.BOLD);
		}
		boldFont = new Font(Display.getDefault(), fontDatas);
	}

	@Override
	protected void initialize(ColumnViewer viewer, ViewerColumn column)
	{
		super.initialize(viewer, column);
		this.viewer = viewer;
	}

	@Override
	protected void measure(Event event, Object element)
	{
		int lineCount = discoveryLabelProvider.getLineCount();
		if (lineCount > 1)
		{
			String newlines = " "; //$NON-NLS-1$
			for (int i = 1; i < lineCount; i++)
			{
				newlines += "\n "; //$NON-NLS-1$
			}
			event.height = event.gc.textExtent(newlines).y + 2;
		}
	}

	@Override
	protected void paint(Event event, Object element)
	{
		if (element instanceof IDiscoveryResult)
		{
			//TODO support LoadingDiscoveryResult (and error?) specifically

			IDiscoveryResult result = (IDiscoveryResult) element;
			String title = discoveryLabelProvider.getTitle(result);
			String description = discoveryLabelProvider.getDescription(result);

			if (title == null && description == null)
			{
				title = result.toString();
			}

			Rectangle clientArea = ((Table) viewer.getControl()).getClientArea();
			int width = clientArea.width - event.x;
			int height = event.height;

			int offset = 0;
			if (title != null)
			{
				TextLayout titleLayout =
						UIUtil.shortenText(event.display, title, boldFont, width, SWT.DEFAULT, false, true);
				titleLayout.draw(event.gc, event.x, event.y);
				offset += titleLayout.getBounds().height;
			}

			if (description != null)
			{
				if (height - offset > 0)
				{
					TextLayout descriptionLayout =
							UIUtil.shortenText(event.display, description, normalFont, width, height - offset, true,
									true);
					descriptionLayout.draw(event.gc, event.x, event.y + offset);
				}
			}
		}
	}


}
