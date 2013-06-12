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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;

import au.gov.ga.earthsci.common.ui.information.TableViewerInformationProvider;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;
import au.gov.ga.earthsci.jface.extras.information.html.HTMLPrinter;

/**
 * Information provider for the tooltip information control for discovery
 * results.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryResultInformationProvider extends TableViewerInformationProvider
{
	private final IDiscoveryResultLabelProvider labelProvider;
	private final static String STYLESHEET_FILENAME = "DiscoveryResultInformationStyleSheet.css"; //$NON-NLS-1$

	public DiscoveryResultInformationProvider(TableViewer viewer, IDiscoveryResultLabelProvider labelProvider)
	{
		super(viewer);
		this.labelProvider = labelProvider;
	}

	@Override
	public Object getInformation(Point location)
	{
		Object information = super.getInformation(location);
		if (information instanceof IDiscoveryResult)
		{
			IDiscoveryResult result = (IDiscoveryResult) information;
			String tooltip = labelProvider.getToolTip(result);
			StringBuffer sb = new StringBuffer(tooltip);
			//add the HTML prefix/suffix to style the HTML like a tooltip:
			HTMLPrinter.insertPageProlog(sb, 0, loadStyleSheet(STYLESHEET_FILENAME));
			HTMLPrinter.addPageEpilog(sb);
			return sb.toString();
		}
		return information;
	}

	/**
	 * Load the named style sheet resource to a string.
	 * 
	 * @param styleSheetName
	 *            Style sheet filename to load
	 * @return Style sheet content as a string
	 */
	public static String loadStyleSheet(String styleSheetName)
	{
		InputStream stream = DiscoveryResultInformationProvider.class.getResourceAsStream(styleSheetName);
		if (stream == null)
		{
			return null;
		}

		try
		{
			String sheet = Util.readStreamToString(stream, "UTF-8"); //$NON-NLS-1$
			FontData fontData = JFaceResources.getFontRegistry().getFontData(JFaceResources.DIALOG_FONT)[0];
			return HTMLPrinter.convertTopLevelFont(sheet, fontData);
		}
		catch (IOException ex)
		{
			return ""; //$NON-NLS-1$
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				//ignore
			}
		}
	}
}
