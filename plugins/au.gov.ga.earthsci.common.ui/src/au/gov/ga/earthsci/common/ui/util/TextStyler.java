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

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.TextStyle;

/**
 * {@link Styler} implementation that applies properties from a local
 * {@link TextStyle} instance.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TextStyler extends Styler
{
	public TextStyle style = new TextStyle();

	@Override
	public void applyStyles(TextStyle textStyle)
	{
		if (style != null)
		{
			textStyle.font = style.font;
			textStyle.foreground = style.foreground;
			textStyle.background = style.background;
			textStyle.underline = style.underline;
			textStyle.underlineColor = style.underlineColor;
			textStyle.underlineStyle = style.underlineStyle;
			textStyle.strikeout = style.strikeout;
			textStyle.strikeoutColor = style.strikeoutColor;
			textStyle.borderStyle = style.borderStyle;
			textStyle.borderColor = style.borderColor;
			textStyle.metrics = style.metrics;
			textStyle.rise = style.rise;
			textStyle.data = style.data;
		}
	}
}
