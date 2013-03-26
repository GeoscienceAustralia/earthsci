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
package au.gov.ga.earthsci.core.model.layer;

/**
 * Helper class that describes a layer node in HTML.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerNodeDescriber
{
	public static String describe(ILayerTreeNode node)
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
			appendProperty(sb, "URI", layer.getURI()); //$NON-NLS-1$
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

	private static void appendProperty(StringBuilder sb, String label, Object value)
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
