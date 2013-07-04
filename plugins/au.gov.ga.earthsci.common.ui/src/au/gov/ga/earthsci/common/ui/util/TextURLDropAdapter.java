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
package au.gov.ga.earthsci.common.ui.util;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.widgets.Text;

/**
 * Copy of org.eclipse.equinox.internal.p2.ui.dialogs.URLDropAdapter.
 */
public class TextURLDropAdapter extends URLDropAdapter
{
	private final Text text;

	public TextURLDropAdapter(Text text, boolean convertFileToURL)
	{
		super(convertFileToURL);
		this.text = text;
	}

	@Override
	protected void handleDrop(String urlText, DropTargetEvent event)
	{
		text.setText(urlText);
		event.detail = DND.DROP_LINK;
	}
}
