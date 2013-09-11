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
package au.gov.ga.earthsci.common.util;

import java.util.List;
import java.util.Map;

import au.gov.ga.earthsci.common.collection.ArrayListHashMap;

/**
 * Class that parses a URL query string into a key/value map.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class QueryString extends ArrayListHashMap<String, String>
{
	/**
	 * Create a QueryString object from the given query string. The string must
	 * be URL encoded.
	 * 
	 * @param queryString
	 */
	public QueryString(String queryString)
	{
		if (queryString != null)
		{
			int lastIndexOfQuestionMark = queryString.lastIndexOf('?');
			queryString = queryString.substring(lastIndexOfQuestionMark + 1);
			String[] params = queryString.split("&+"); //$NON-NLS-1$
			for (String param : params)
			{
				String[] pair = param.split("=+"); //$NON-NLS-1$
				String key = UTF8URLEncoder.decode(pair[0]);
				String value = pair.length > 1 ? UTF8URLEncoder.decode(pair[1]) : null;
				putSingle(key, value);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, List<String>> entry : entrySet())
		{
			sb.append(", "); //$NON-NLS-1$
			sb.append(entry.getKey());
			sb.append(" = "); //$NON-NLS-1$

			List<String> values = entry.getValue();
			if (values != null && !values.isEmpty())
			{
				if (values.size() == 1)
				{
					sb.append(values.get(0));
				}
				else
				{
					sb.append("["); //$NON-NLS-1$
					for (int i = 0; i < values.size(); i++)
					{
						if (i > 0)
						{
							sb.append(", "); //$NON-NLS-1$
						}
						sb.append(values.get(i));
					}
					sb.append("]"); //$NON-NLS-1$
				}
			}
		}
		String s = sb.length() == 0 ? sb.toString() : sb.substring(2);
		return "[" + s + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
