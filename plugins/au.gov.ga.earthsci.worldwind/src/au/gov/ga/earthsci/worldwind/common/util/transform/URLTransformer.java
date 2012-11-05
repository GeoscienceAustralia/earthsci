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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maintains a static list of {@link URLTransform}'s that can be applied in
 * order using the {@link #transform} methods.
 * <p/>
 * Examples of {@link URLTransform}s might be to change the port number based on
 * the current environment, or append request parameters to URLs matching a
 * given pattern.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URLTransformer
{
	private static List<URLTransform> transforms = new ArrayList<URLTransform>();

	public synchronized static void addTransform(URLTransform transform)
	{
		transforms.add(transform);
	}

	public synchronized static void removeTransform(URLTransform transform)
	{
		transforms.remove(transform);
	}

	public synchronized static URL transform(URL url) throws MalformedURLException
	{
		if (url == null)
			return null;

		return new URL(transform(url.toExternalForm()));
	}

	public synchronized static String transform(String url)
	{
		if (url == null)
			return null;

		for (URLTransform transform : transforms)
		{
			url = transform.transformURL(url);
		}
		return url;
	}

	public synchronized static void clearTransforms()
	{
		transforms.clear();
	}

	public static List<URLTransform> getTransforms()
	{
		return Collections.unmodifiableList(transforms);
	}
}
