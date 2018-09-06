/*******************************************************************************
 * Copyright 2018 Geoscience Australia
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
package net.jeeeyul.eclipse.themes.css;

import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.w3c.dom.css.CSSValue;

/**
 * 
 *
 * @author u24529
 */
public class RGBHelper
{

	private RGBHelper()
	{
	};

	public static RGB getRGB(CSSValue value)
	{
		RGBA rgba = CSSSWTColorHelper.getRGBA(value);
		if (rgba == null)
		{
			return null;
		}
		return rgba.rgb;
	}

}
