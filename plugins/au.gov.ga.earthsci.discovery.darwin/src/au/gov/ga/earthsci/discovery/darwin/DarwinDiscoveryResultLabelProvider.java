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
package au.gov.ga.earthsci.discovery.darwin;

import au.gov.ga.earthsci.common.util.HtmlUtil;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultLabelProvider;

/**
 * {@link IDiscoveryResultLabelProvider} implementation for DARWIN results.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscoveryResultLabelProvider implements IDiscoveryResultLabelProvider
{
	@Override
	public int getLineCount()
	{
		return 4;
	}

	@Override
	public String getTitle(IDiscoveryResult result)
	{
		return ((DarwinDiscoveryResult) result).getTitle();
	}

	@Override
	public String getDescription(IDiscoveryResult result)
	{
		return ((DarwinDiscoveryResult) result).getDescription();
	}

	@Override
	public String getToolTip(IDiscoveryResult result)
	{
		DarwinDiscoveryResult darwinResult = (DarwinDiscoveryResult) result;

		String title = HtmlUtil.convertToHTMLContent(darwinResult.getTitle());
		String description = HtmlUtil.convertToHTMLContentWithWhitespace(darwinResult.getDescription());
		String html = "<h3 style='margin-top: 0'>" + title + "</h3>" + description; //$NON-NLS-1$ //$NON-NLS-2$
		return html;
	}
}
