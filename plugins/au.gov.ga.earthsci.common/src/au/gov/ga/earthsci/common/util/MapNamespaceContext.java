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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * XML {@link NamespaceContext} implementation that uses an internal map of XML
 * namespace <code>prefix</code>es to <code>namespaceURI</code>s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MapNamespaceContext implements NamespaceContext
{
	private Map<String, String> prefixToURI = new HashMap<String, String>();
	private Map<String, String> uriToPrefix = new HashMap<String, String>();

	public void add(String prefix, String namespaceURI)
	{
		prefixToURI.put(prefix, namespaceURI);
		uriToPrefix.put(namespaceURI, prefix);
	}

	public void remove(String prefix)
	{
		String namespaceURI = prefixToURI.remove(prefix);
		uriToPrefix.remove(namespaceURI);
	}

	@Override
	public String getNamespaceURI(String prefix)
	{
		return prefixToURI.get(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI)
	{
		return uriToPrefix.get(namespaceURI);
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI)
	{
		return Arrays.asList(new String[] { getPrefix(namespaceURI) }).iterator();
	}
}
