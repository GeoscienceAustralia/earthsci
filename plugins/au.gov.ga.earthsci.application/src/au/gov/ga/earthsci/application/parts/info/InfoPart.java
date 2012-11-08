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
package au.gov.ga.earthsci.application.parts.info;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;

/**
 * Part that shows information about the currently selected item.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InfoPart
{
	private Browser browser;

	@Inject
	public void init(Composite parent)
	{
		browser = new Browser(parent, SWT.NONE);
	}

	@Inject
	public void selectLayer(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		if (layer != null)
		{
			if (layer.getInfoURL() != null)
			{
				browser.setUrl(layer.getInfoURL().toString());
			}
			else
			{
				String html = generateInfoHtml(layer);
				browser.setText(html);
			}
		}
	}

	private String generateInfoHtml(ILayerTreeNode node)
	{
		String layerOrFolder = node instanceof FolderNode ? "Folder" : "Layer";
		StringBuilder sb = new StringBuilder();
		sb.append("<html>"); //$NON-NLS-1$
		sb.append("<body>"); //$NON-NLS-1$
		sb.append("<h1>"); //$NON-NLS-1$
		sb.append(layerOrFolder);
		sb.append(" "); //$NON-NLS-1$
		sb.append("details");
		sb.append("</h1>"); //$NON-NLS-1$
		appendProperty(sb, "Name", node.getName());
		appendProperty(sb, "Label", node.getLabel());
		if (node instanceof LayerNode)
		{
			LayerNode layer = (LayerNode) node;
			appendProperty(sb, "URI", layer.getLayerURI()); //$NON-NLS-1$
		}
		if (node.getLegendURL() != null)
		{
			String url = node.getLegendURL().toString();
			appendProperty(sb, "Legend", "<a href=\"" + url + "\">" + url + "</a>"); //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		sb.append("</body>"); //$NON-NLS-1$
		sb.append("</html>"); //$NON-NLS-1$
		return sb.toString();
	}

	private void appendProperty(StringBuilder sb, String label, Object value)
	{
		if (value != null)
		{
			sb.append("<b>"); //$NON-NLS-1$
			sb.append(label);
			sb.append(": "); //$NON-NLS-1$
			sb.append("</b>"); //$NON-NLS-1$
			sb.append(value);
			sb.append("<br />"); //$NON-NLS-1$
		}
	}
}
