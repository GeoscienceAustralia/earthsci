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
package au.gov.ga.earthsci.worldwind.common.util.transform;

/**
 * {@link URLTransform} implementation that transforms urls by searching for a
 * particular regular expression and replacing it with the provided replacement.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RegexURLTransform implements URLTransform
{
	private final String regex;
	private final String replacement;

	public RegexURLTransform(String regex, String replacement)
	{
		this.regex = regex;
		this.replacement = replacement;
	}

	@Override
	public String transformURL(String url)
	{
		if (url == null)
		{
			return url;
		}
		return url.replaceAll(regex, replacement);
	}
}
