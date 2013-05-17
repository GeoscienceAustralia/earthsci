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
package au.gov.ga.earthsci.common.util;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods for working with URI objects
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class URIUtil
{
	private URIUtil()
	{
	}

	@SuppressWarnings("nls")
	public static Map<String, String> getParameterMap(URI uri)
	{
		if (Util.isEmpty(uri.getQuery()))
		{
			return Collections.emptyMap();
		}

		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String paramPair : uri.getQuery().split("[&;]+"))
		{
			String[] paramComponents = paramPair.split("=");
			if (paramComponents.length > 1)
			{
				result.put(paramComponents[0], paramComponents[1]);
			}
			else if (paramComponents.length == 1)
			{
				result.put(paramComponents[0], null);
			}
		}
		return result;
	}
}
