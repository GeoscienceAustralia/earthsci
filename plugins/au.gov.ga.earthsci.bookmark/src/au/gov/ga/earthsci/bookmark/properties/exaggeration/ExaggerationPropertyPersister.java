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
package au.gov.ga.earthsci.bookmark.properties.exaggeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.persistence.PersistenceException;
import au.gov.ga.earthsci.common.persistence.Persister;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;

/**
 * An {@link IBookmarkPropertyCreator} that can create a
 * {@link ExaggerationProperty} instance
 * 
 * @author Michael de Hoog
 */
public class ExaggerationPropertyPersister implements IBookmarkPropertyCreator, IBookmarkPropertyExporter
{
	private static final String EXAGGERATION_ELEMENT_NAME = "exaggeration"; //$NON-NLS-1$

	private static final String[] SUPPORTED_TYPES = new String[] { ExaggerationProperty.TYPE };

	private static final Logger logger = LoggerFactory.getLogger(ExaggerationPropertyPersister.class);

	private Persister persister;

	public ExaggerationPropertyPersister()
	{
		persister = new Persister();
		persister.setIgnoreMissing(false);
		persister.setIgnoreNulls(false);
		persister.registerNamedExportable(ExaggerationProperty.class, EXAGGERATION_ELEMENT_NAME);
	}

	@Override
	public String[] getSupportedTypes()
	{
		return SUPPORTED_TYPES;
	}

	@Override
	public IBookmarkProperty createFromCurrentState(String type)
	{
		double exaggeration = VerticalExaggerationService.INSTANCE.get();
		return new ExaggerationProperty(exaggeration);
	}

	@Override
	public void exportToXML(IBookmarkProperty property, Element propertyElement)
	{
		if (property == null)
		{
			return;
		}
		Validate.isTrue(property.getType().equals(ExaggerationProperty.TYPE),
				"ExaggerationPropertyPersister can only be used for exaggeration properties"); //$NON-NLS-1$
		Validate.notNull(propertyElement, "A property element is required"); //$NON-NLS-1$
		try
		{
			persister.save(property, propertyElement, null);
		}
		catch (PersistenceException e)
		{
			logger.error("Exception while saving camera bookmark property", e); //$NON-NLS-1$
		}
	}

	@Override
	public IBookmarkProperty createFromXML(String type, Element propertyElement)
	{
		Validate.isTrue(ExaggerationProperty.TYPE.equals(type),
				"ExaggerationPropertyPersister can only be used for exaggeration properties"); //$NON-NLS-1$
		if (propertyElement == null)
		{
			return null;
		}
		try
		{
			return (ExaggerationProperty) persister.load(
					XmlUtil.getChildElementByTagName(0, EXAGGERATION_ELEMENT_NAME, propertyElement), null);
		}
		catch (PersistenceException e)
		{
			logger.error("Exception while loading exaggeration bookmark property", e); //$NON-NLS-1$
		}
		return null;
	}
}
