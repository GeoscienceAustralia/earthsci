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
package au.gov.ga.earthsci.catalog;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.seeder.ISeeder;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Seeder that can remove catalogs from the catalog model.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CatalogRemoveSeeder implements ISeeder
{
	@Inject
	private ICatalogModel catalogModel;

	@Override
	public void seed(Element element, URL context)
	{
		Element[] children = XmlUtil.getElements(element);
		for (Element child : children)
		{
			String uriString = child.getAttribute("uri"); //$NON-NLS-1$
			if (!Util.isBlank(uriString))
			{
				try
				{
					URI uri = new URI(uriString);
					catalogModel.removeTopLevelCatalogsForURI(uri);
				}
				catch (URISyntaxException e)
				{
				}
			}
		}
	}
}
