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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

/**
 * Utility class for general UI methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class UIUtil
{
	//could use the ellipsis glyph on some platforms "\u2026"
	private static final String ELLIPSIS = "..."; //$NON-NLS-1$

	/**
	 * Create a TextLayout that contains the given text, shorted to fit within
	 * the given width (and optional height).
	 * 
	 * @param device
	 *            Current device
	 * @param text
	 *            Text to shorten
	 * @param font
	 *            Font to use when calculating shortened text
	 * @param maxWidth
	 *            Maximum width available for text; if {@link SWT#DEFAULT}, text
	 *            is unshortened
	 * @param maxHeight
	 *            Maximum height available for text; only used if multiline is
	 *            <code>true</code>
	 * @param multiline
	 *            Should the text be wrapped over multiple lines
	 * @param addEllipsis
	 *            Should an ellipsis be added on the end if the text requires
	 *            shortening
	 * @return TextLayout containing the shortened and constrained text
	 */
	public static TextLayout shortenText(Device device, String text, Font font, int maxWidth, int maxHeight,
			boolean multiline, boolean addEllipsis)
	{
		TextLayout layout = new TextLayout(device);
		layout.setText(text);
		layout.setFont(font);

		//multiline can fit into any width, so if no maxHeight is defined return unshortened
		if (maxWidth == SWT.DEFAULT || (multiline && maxHeight == SWT.DEFAULT))
		{
			return layout;
		}

		if (multiline)
		{
			layout.setWidth(maxWidth);
		}

		Rectangle bounds = layout.getBounds();
		boolean fits = multiline ? (bounds.height <= maxHeight) : (bounds.width <= maxWidth);
		if (fits)
		{
			return layout;
		}

		int end = 0;
		while (true)
		{
			int nextEnd = layout.getNextOffset(end, SWT.MOVEMENT_CLUSTER);
			bounds = layout.getBounds(0, nextEnd);
			fits = multiline ? (bounds.height <= maxHeight) : (bounds.width <= maxWidth);
			if (!fits)
			{
				break;
			}
			end = nextEnd;
		}

		String shortenedText = text.substring(0, end);
		while (true)
		{
			String textWithSuffix = shortenedText;
			if (addEllipsis)
			{
				textWithSuffix += ELLIPSIS;
			}
			layout.setText(textWithSuffix);
			bounds = layout.getBounds();
			fits = multiline ? (bounds.height <= maxHeight) : (bounds.width <= maxWidth);
			if (fits || shortenedText.length() == 0)
			{
				return layout;
			}
			shortenedText = shortenedText.substring(0, shortenedText.length() - 1);
		}
	}
}
