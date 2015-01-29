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

import java.net.URL;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.seeder.ISeeder;
import au.gov.ga.earthsci.seeder.SeederManager;

/**
 * Seeder for the catalog model.
 * 
 * @see SeederManager
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CatalogSeeder implements ISeeder
{
	private static final Logger logger = LoggerFactory.getLogger(CatalogSeeder.class);

	@Inject
	private ICatalogModel catalogModel;

	@Inject
	private IEclipseContext context;

	@Override
	public void seed(Element element, URL context)
	{
		Element[] children = XmlUtil.getElements(element);
		for (Element child : children)
		{
			Element[] children2 = XmlUtil.getElements(child);
			for (Element child2 : children2)
			{
				try
				{
					CatalogPersister.loadCatalogModel(child2, context.toURI(), catalogModel, this.context, true);
				}
				catch (Exception e)
				{
					logger.error("Error unpersisting catalogs from seed file", e); //$NON-NLS-1$
				}
			}
		}
	}
}
