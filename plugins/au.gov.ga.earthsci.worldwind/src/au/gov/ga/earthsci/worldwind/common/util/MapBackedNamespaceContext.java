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
package au.gov.ga.earthsci.worldwind.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A simple implementation of the {@link NamespaceContext} that maintains a map of prefix->URI mappings
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class MapBackedNamespaceContext implements NamespaceContext
{
	private Map<String, String> prefixToUriMap = new HashMap<String, String>();
	private Map<String, String> uriToPrefixMap = new HashMap<String, String>();

	public void addMapping(String prefix, String uri)
	{
		prefixToUriMap.put(prefix, uri);
		uriToPrefixMap.put(uri, prefix);
	}
	
	@Override
	public String getNamespaceURI(String prefix)
	{
		String result = prefixToUriMap.get(prefix);
		if (result != null)
		{
			return result;
		}
		return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI)
	{
		return uriToPrefixMap.get(namespaceURI);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getPrefixes(String namespaceURI)
	{
		return prefixToUriMap.keySet().iterator();
	}
	
}
