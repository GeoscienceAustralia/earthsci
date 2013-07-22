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

import au.gov.ga.earthsci.common.util.HtmlUtil;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;

/**
 * Label provider for the CSW discovery implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWDiscoveryResultLabelProvider implements IDiscoveryResultLabelProvider
{
	@Override
	public String getTitle(IDiscoveryResult result)
	{
		return ((CSWDiscoveryResult) result).getTitle();
	}

	@Override
	public String getDescription(IDiscoveryResult result)
	{
		return ((CSWDiscoveryResult) result).getDescription();
	}

	@Override
	public String getToolTip(IDiscoveryResult result)
	{
		CSWDiscoveryResult cswResult = (CSWDiscoveryResult) result;
		String title = HtmlUtil.convertToHTMLContent(cswResult.getTitle());
		String description = HtmlUtil.convertToHTMLContentWithWhitespace(cswResult.getDescription());
		String html = "<h3 style='margin-top: 0'>" + title + "</h3>" + description; //$NON-NLS-1$ //$NON-NLS-2$
		return html;
	}

	@Override
	public int getLineCount()
	{
		return 4;
	}
}
